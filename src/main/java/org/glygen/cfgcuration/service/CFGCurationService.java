package org.glygen.cfgcuration.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.glygen.cfgcuration.NamespaceHandler;
import org.glygen.cfgcuration.dao.BiologicalRepository;
import org.glygen.cfgcuration.dao.MappingDiseaseRepository;
import org.glygen.cfgcuration.dao.MappingOrganRepository;
import org.glygen.cfgcuration.dao.MappingSpeciesRepository;
import org.glygen.cfgcuration.dao.MappingTissueRepository;
import org.glygen.cfgcuration.dao.PublicationRepository;
import org.glygen.cfgcuration.dao.StructureRepository;
import org.glygen.cfgcuration.model.NamespaceEntry;
import org.glygen.cfgcuration.model.Publication;
import org.glygen.cfgcuration.model.Species;
import org.glygen.cfgcuration.model.Structures;
import org.glygen.cfgcuration.model.mapping.MappingDisease;
import org.glygen.cfgcuration.model.mapping.MappingOrgan;
import org.glygen.cfgcuration.model.mapping.MappingScientificName;
import org.glygen.cfgcuration.model.mapping.MappingTissue;
import org.glygen.cfgcuration.util.PubmedUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.transaction.Transactional;


@Service
public class CFGCurationService {
	
	@Value("${ncbi.api-key}")
	String apiKey;
	
	static Logger logger = org.slf4j.LoggerFactory.getLogger(CFGCurationService.class);
	
	public class CFGRecord {
		String carbKey;
		String linearCode;
	}

	private final BiologicalRepository bioRepository;
	private final StructureRepository structureRepository;
	private final PublicationRepository publicationRepository;
	private final MappingDiseaseRepository mappingDiseaseRepository;
	private final MappingTissueRepository mappingTissueRepository;
	private final MappingSpeciesRepository mappingSpeciesRepository;
	private final MappingOrganRepository mappingOrganRepository;
	
	public CFGCurationService(StructureRepository structureRepository, BiologicalRepository bioRepository, 
			PublicationRepository publicationRepository, 
			MappingTissueRepository mappingTissueRepository, 
			MappingSpeciesRepository mappingSpeciesRepository, 
			MappingOrganRepository mappingOrganRepository, 
			MappingDiseaseRepository mappingDiseaseRepository) {
		this.bioRepository = bioRepository;
		this.structureRepository = structureRepository;
		this.publicationRepository = publicationRepository;
		this.mappingDiseaseRepository = mappingDiseaseRepository;
		this.mappingTissueRepository = mappingTissueRepository;
		this.mappingSpeciesRepository = mappingSpeciesRepository;
		this.mappingOrganRepository = mappingOrganRepository;
	}
	
	public void assignCarbKeys () {
		try {
			Map<String, CFGRecord> carbIdMap = new HashMap<>();
            File inputFile = new File("carbohydrate.XML"); 
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("DATA_RECORD");

            for (int temp = 0; temp < nList.getLength(); temp++) {
            	Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                	Element eElement = (Element) nNode;
                    String carbId = eElement.getElementsByTagName("CARB_ID").item(0).getTextContent();
                    String carbKey = eElement.getElementsByTagName("CARB_KEY").item(0).getTextContent();
                    NodeList n = eElement.getElementsByTagName("CARB_LINEARCODE");
                    String linearCode = null;
                    if (n != null && n.getLength() > 0) {
                    	linearCode = n.item(0).getTextContent();
                    }
                    CFGRecord rec = new CFGRecord();
                    rec.carbKey = carbKey;
                    rec.linearCode = linearCode;
                    carbIdMap.put(carbId, rec);
                }
            }
            
            StringBuffer errors = new StringBuffer();
            List<Structures> records = structureRepository.findAll();
    		for (Structures str: records) {
    			CFGRecord rec = carbIdMap.get(str.getCarb_id());
    			if (rec == null) {
    				errors.append ("Cannot find carb key in XML for carb id: " + str.getCarb_id() + "\n");
    				continue;
    			} 
    			if (rec.linearCode != null && rec.linearCode.equalsIgnoreCase(str.getLinearcode())) {
    				if (str.getCarb_key() != null && !str.getCarb_key().equalsIgnoreCase(rec.carbKey)) {
    					errors.append("Row with carbId " + str.getCarb_id() + " already has carbKey assigned " + str.getCarb_key() + "\n");
    				} else if (str.getCarb_key() == null) {
    					str.setCarb_key(rec.carbKey);
    					structureRepository.save(str);
    				}
    			} else {
    				errors.append("Row with carbId " + str.getCarb_id() + " has linearcode: " 
    						+ str.getLinearcode() + " and it does not match XML file: " +  rec.linearCode + "\n");
    			}
    		}
    		
	        String filePath = "error_log.txt"; 
	        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
            writer.write(errors.toString());
            writer.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void createPublications () {
		
		try {
			if (publicationRepository.count() > 0)
				return;
			
			File inputFile = new File("carb_references.XML"); 
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(inputFile);
	        doc.getDocumentElement().normalize();
	
	        NodeList nList = doc.getElementsByTagName("DATA_RECORD");
	        for (int temp = 0; temp < nList.getLength(); temp++) {
            	Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                	Element eElement = (Element) nNode;
                	NodeList n1 = eElement.getElementsByTagName("CARB_KEY");
                	NodeList n2 = eElement.getElementsByTagName("JOURNAL_KEY");
                	NodeList n3 = eElement.getElementsByTagName("JOURNAL_ID");
                	NodeList n4 = eElement.getElementsByTagName("TITLE");
                	NodeList n5 = eElement.getElementsByTagName("PAGE_RANGE");
                	NodeList n6 = eElement.getElementsByTagName("JOURNAL_NAME");
                	NodeList n7 = eElement.getElementsByTagName("YEAR");
                	NodeList n8 = eElement.getElementsByTagName("JOURNAL_IDTYPE");
                	NodeList n9 = eElement.getElementsByTagName("AUTHOR");
                	NodeList n10 = eElement.getElementsByTagName("VOLUME");
                	
                    Publication pub = new Publication();
                    if (n1 != null && n1.getLength() > 0) pub.setCarbKey(n1.item(0).getTextContent());
                    if (n2 != null && n2.getLength() > 0) pub.setJournalKey(n2.item(0).getTextContent());
                    if (n3 != null && n3.getLength() > 0) pub.setJournalId(n3.item(0).getTextContent());
                    if (n4 != null && n4.getLength() > 0) pub.setTitle(n4.item(0).getTextContent());
                    if (n5 != null && n5.getLength() > 0) pub.setPageRange(n5.item(0).getTextContent());
                    if (n6 != null && n6.getLength() > 0) pub.setJournalName(n6.item(0).getTextContent());
                    if (n7 != null && n7.getLength() > 0) pub.setYear(n7.item(0).getTextContent());
                    if (n8 != null && n8.getLength() > 0) pub.setJournalIdType(n8.item(0).getTextContent());
                    if (n9 != null && n9.getLength() > 0) pub.setAuthor(n9.item(0).getTextContent());
                    if (n10 != null && n10.getLength() > 0) pub.setVolume(n10.item(0).getTextContent());
                    publicationRepository.save(pub);

                }
	        }
	        
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@Transactional
	public void createMappingTables () {
		long c = mappingSpeciesRepository.count();
		if (c == 0) {
			List<String> distinctValues = bioRepository.findDistinctScientificname();
			for (String name: distinctValues) {
				long count = bioRepository.countByScientificnameIgnoreCase(name);
				MappingScientificName mapping = new MappingScientificName();
				mapping.setCount(Long.valueOf(count).intValue());
				mapping.setName(name);
				
				mappingSpeciesRepository.save(mapping);
			}
		}
		
		c = mappingTissueRepository.count();
		if (c == 0) {
			List<String> distinctValues = bioRepository.findDistinctTissue();
			for (String name: distinctValues) {
				long count = bioRepository.countByTissueIgnoreCase(name);
				MappingTissue mapping = new MappingTissue();
				mapping.setCount(Long.valueOf(count).intValue());
				mapping.setName(name);
				
				mappingTissueRepository.save(mapping);
			}
		}
		
		c = mappingOrganRepository.count();
		if (c == 0) {
			List<String> distinctValues = bioRepository.findDistinctOrgan();
			for (String name: distinctValues) {
				long count = bioRepository.countByOrganIgnoreCase(name);
				MappingOrgan mapping = new MappingOrgan();
				mapping.setCount(Long.valueOf(count).intValue());
				mapping.setName(name);
				
				mappingOrganRepository.save(mapping);
			}
		}
		
		c = mappingDiseaseRepository.count();
		if (c == 0) {
			List<String> distinctValues = bioRepository.findDistinctDisease();
			for (String name: distinctValues) {
				long count = bioRepository.countByDiseaseIgnoreCase(name);
				MappingDisease mapping = new MappingDisease();
				mapping.setCount(Long.valueOf(count).intValue());
				mapping.setName(name);
				
				mappingDiseaseRepository.save(mapping);
			}
		}
	}
	
	public void addInformationToMappingTables () {
		PubmedUtil util = new PubmedUtil(apiKey);
		
		List<MappingScientificName> allBS = mappingSpeciesRepository.findAll();
		for (MappingScientificName bs: allBS) {
			try {
				if (bs.getNamespaceName() == null) {
					if (bs.getName() == null) continue;
					List<Species> matches = util.getSpecies(bs.getName());
					if (!matches.isEmpty()) {
						if (matches.size() > 1) {
							logger.info("multiple matches for " + bs.getName());
						} else {
							Species s = matches.get(0);
							bs.setNamespaceName(s.getName());
							bs.setRank(s.getRank());
							bs.setNamespaceId(s.getId());
							mappingSpeciesRepository.save(bs);
						}
					}
				}
				
				try {
			        Thread.sleep(100); // wait 100 milliseconds between requests
			    } catch (InterruptedException e) {
			        Thread.currentThread().interrupt(); // restore interrupted status
			    }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<MappingDisease> allDisease = mappingDiseaseRepository.findAll();
		for (MappingDisease d: allDisease) {
			if (d.getNamespaceName() == null && d.getMatchCount() == null) {
				if (d.getName() == null) continue;
				List<NamespaceEntry> matches = findCanonicalForm("doid-base.txt", d.getName());
				d.setMatchCount(matches.size()+"");
				if (!matches.isEmpty()) {
					if (matches.size() > 1) {
						logger.info("multiple matches for " + d.getName());
					} else {
						NamespaceEntry match = matches.get(0);
						d.setNamespaceName(match.getLabel());
						if (!d.getName().equalsIgnoreCase(match.getLabel())) {
							d.setMappingName(d.getName());
						}
						if (match.getUri() != null) {
							String id = match.getUri().substring(match.getUri().lastIndexOf("/")+1);
							String[] split = id.split("_");
							String namespaceId = split[0] + (split.length > 1 ? ":" + split[1] : "");
							d.setNamespaceId(namespaceId);
						}
						
					}
				}
				mappingDiseaseRepository.save(d);
			}
		}
		
		List<MappingTissue> allOT = mappingTissueRepository.findAll();
		for (MappingTissue d: allOT) {
			if (d.getNamespaceName() == null && d.getMatchCount() == null) {
				if (d.getName() == null) continue;
				List<NamespaceEntry> matches = findCanonicalForm("uberon-base.txt", d.getName());
				d.setMatchCount(matches.size()+"");
				if (!matches.isEmpty()) {
					if (matches.size() > 1) {
						logger.info("multiple matches for " + d.getName());
					} else {
						NamespaceEntry match = matches.get(0);
						d.setNamespaceName(match.getLabel());
						if (!d.getName().equalsIgnoreCase(match.getLabel())) {
							d.setMappingName(d.getName());
						}
						if (match.getUri() != null) {
							String id = match.getUri().substring(match.getUri().lastIndexOf("/")+1);
							String[] split = id.split("_");
							String namespaceId = split[0] + (split.length > 1 ? ":" + split[1] : "");
							d.setNamespaceId(namespaceId);
						}
					}
				}
				mappingTissueRepository.save(d);
			}
		}
		
		List<MappingOrgan> allOr = mappingOrganRepository.findAll();
		for (MappingOrgan d: allOr) {
			if (d.getNamespaceName() == null && d.getMatchCount() == null) {
				if (d.getName() == null) continue;
				List<NamespaceEntry> matches = findCanonicalForm("uberon-base.txt", d.getName());
				d.setMatchCount(matches.size()+"");
				if (!matches.isEmpty()) {
					if (matches.size() > 1) {
						logger.info("multiple matches for " + d.getName());
					} else {
						NamespaceEntry match = matches.get(0);
						d.setNamespaceName(match.getLabel());
						if (!d.getName().equalsIgnoreCase(match.getLabel())) {
							d.setMappingName(d.getName());
						}
						if (match.getUri() != null) {
							String id = match.getUri().substring(match.getUri().lastIndexOf("/")+1);
							String[] split = id.split("_");
							String namespaceId = split[0] + (split.length > 1 ? ":" + split[1] : "");
							d.setNamespaceId(namespaceId);
						}
					}
				}
				mappingOrganRepository.save(d);
			}
		}
	}
	
	List<NamespaceEntry> findCanonicalForm (String namespaceFile, String value) {
		// find the file identifier associated with the given namespace
				
		List<NamespaceEntry> matches = new ArrayList<>();
		PatriciaTrie<List<NamespaceEntry>> trie = null;
		
		// find the exact match if exists
		trie = NamespaceHandler.getTrieForNamespace(namespaceFile);
		if (trie != null) {
			Entry<String, List<NamespaceEntry>> entry = trie.select(value.toLowerCase());
			if (entry.getKey().toLowerCase().equals(value.toLowerCase())) {
				matches.addAll(entry.getValue());
			}
		}
		
		return matches;
	}
}
