package org.glygen.cfgcuration.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glygen.cfgcuration.NamespaceHandler;
import org.glygen.cfgcuration.dao.BiologicalRepository;
import org.glygen.cfgcuration.dao.MappingDiseaseRepository;
import org.glygen.cfgcuration.dao.MappingOrganRepository;
import org.glygen.cfgcuration.dao.MappingSpeciesRepository;
import org.glygen.cfgcuration.dao.MappingTissueRepository;
import org.glygen.cfgcuration.dao.PublicationRepository;
import org.glygen.cfgcuration.dao.StructureRepository;
import org.glygen.cfgcuration.model.Biological;
import org.glygen.cfgcuration.model.NamespaceEntry;
import org.glygen.cfgcuration.model.Publication;
import org.glygen.cfgcuration.model.Species;
import org.glygen.cfgcuration.model.Structures;
import org.glygen.cfgcuration.model.mapping.Mapping;
import org.glygen.cfgcuration.model.mapping.MappingDisease;
import org.glygen.cfgcuration.model.mapping.MappingOrgan;
import org.glygen.cfgcuration.model.mapping.MappingScientificName;
import org.glygen.cfgcuration.model.mapping.MappingTissue;
import org.glygen.cfgcuration.util.CrossRefAPI;
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
	
	@Transactional
	public void updateMappings (List<Mapping> mappings, String tablename) {
		PubmedUtil util = new PubmedUtil(apiKey);
		for (Mapping mapping: mappings) {
			if (tablename.equalsIgnoreCase("mapping_scientificname")) {
				try {
					Species species = util.getSpeciesByID(mapping.getNamespaceId());
					Optional<MappingScientificName> record = mappingSpeciesRepository.findById(mapping.getId());
					if (record.isPresent()) {
						MappingScientificName existing = record.get();
						if (!existing.getName().equalsIgnoreCase(species.getName())) {
							existing.setMappingName(existing.getName());
						}
						existing.setInProgress(mapping.getInProgress());
						existing.setNamespaceId(mapping.getNamespaceId());
						existing.setNamespaceName(species.getName());
						existing.setRank(species.getRank());
						mappingSpeciesRepository.save(existing);
					}
				} catch (IOException e) {
					logger.error("Could not retrive species from NCBI Taxonomy for: " + mapping.getNamespaceId(), e);
				}
			} else if (tablename.equalsIgnoreCase("mapping_disease")) {
				//TODO find the canonical form etc from the dictionary, not from the mapping
				Optional<MappingDisease> record = mappingDiseaseRepository.findById(mapping.getId());
				if (record.isPresent()) {
					MappingDisease existing = record.get();
					existing.setInProgress(mapping.getInProgress());
					existing.setMappingName(mapping.getMappingName());
					existing.setNamespaceId(mapping.getNamespaceId());
					existing.setNamespaceName(mapping.getNamespaceName());
					mappingDiseaseRepository.save(existing);
				}
			} else if (tablename.equalsIgnoreCase("mapping_tissue")) {
				//TODO find the canonical form etc from the dictionary, not from the mapping
				Optional<MappingTissue> record = mappingTissueRepository.findById(mapping.getId());
				if (record.isPresent()) {
					MappingTissue existing = record.get();
					existing.setInProgress(mapping.getInProgress());
					existing.setMappingName(mapping.getMappingName());
					existing.setNamespaceId(mapping.getNamespaceId());
					existing.setNamespaceName(mapping.getNamespaceName());
					mappingTissueRepository.save(existing);
				}
			} else if (tablename.equalsIgnoreCase("mapping_organ")) {
				//TODO find the canonical form etc from the dictionary, not from the mapping
				Optional<MappingOrgan> record = mappingOrganRepository.findById(mapping.getId());
				if (record.isPresent()) {
					MappingOrgan existing = record.get();
					existing.setInProgress(mapping.getInProgress());
					existing.setMappingName(mapping.getMappingName());
					existing.setNamespaceId(mapping.getNamespaceId());
					existing.setNamespaceName(mapping.getNamespaceName());
					mappingOrganRepository.save(existing);
				}
			} else {
				logger.error("Comparison of " + tablename + " has not been implemented yet!");
			}
		}
		
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
	
	        List <Publication> created = new ArrayList<>();
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
                    if (!created.contains(pub)) {
                    	created.add(pub);
                    	publicationRepository.save(pub);
                    }
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
			Set<String> alreadyAdded = new HashSet<>();
			Map <String, Integer> counts = new HashMap<>();
			List<String> distinctValues = bioRepository.findDistinctTissue();
			for (String name: distinctValues) {
				if (name != null) {
					String[] multiple = name.split(",");
					for (String n: multiple) {
						if (!alreadyAdded.contains(n.trim())) {
							alreadyAdded.add(n.trim());
							MappingTissue mapping = new MappingTissue();
							mapping.setName(n.trim());
							mappingTissueRepository.save(mapping);
						}
						if (counts.get(n.trim()) == null) {
							counts.put(n.trim(), 1);
						} else {
							counts.put(n.trim(), counts.get(n.trim()) + 1);
						}
					}
				}
			}
			
			List<MappingTissue> mappings = mappingTissueRepository.findAll();
			for (MappingTissue mapping: mappings) {
				Integer count = counts.get(mapping.getName());
				if (count != null) {
					mapping.setCount(count);
					mappingTissueRepository.save(mapping);
				}
			}
		}
		
		c = mappingOrganRepository.count();
		if (c == 0) {
			Set<String> alreadyAdded = new HashSet<>();
			Map <String, Integer> counts = new HashMap<>();
			List<String> distinctValues = bioRepository.findDistinctOrgan();
			for (String name: distinctValues) {
				if (name != null) {
					String[] multiple = name.split(",");
					for (String n: multiple) {
						if (!alreadyAdded.contains(n.trim())) {
							alreadyAdded.add(n.trim());
							MappingOrgan mapping = new MappingOrgan();
							mapping.setName(n.trim());
							
							mappingOrganRepository.save(mapping);
						}
						if (counts.get(n.trim()) == null) {
							counts.put(n.trim(), 1);
						} else {
							counts.put(n.trim(), counts.get(n.trim()) + 1);
						}
					}
				}
			}
			List<MappingOrgan> mappings = mappingOrganRepository.findAll();
			for (MappingOrgan mapping: mappings) {
				Integer count = counts.get(mapping.getName());
				if (count != null) {
					mapping.setCount(count);
					mappingOrganRepository.save(mapping);
				}
			}
		}
		
		c = mappingDiseaseRepository.count();
		if (c == 0) {
			Set<String> alreadyAdded = new HashSet<>();
			Map <String, Integer> counts = new HashMap<>();
			List<String> distinctValues = bioRepository.findDistinctDisease();
			for (String name: distinctValues) {
				if (name != null) {
					String[] multiple = name.split(",");
					for (String n: multiple) {
						if (!alreadyAdded.contains(n.trim())) {
							alreadyAdded.add(n.trim());
							MappingDisease mapping = new MappingDisease();
							mapping.setName(n.trim());
							mappingDiseaseRepository.save(mapping);
						}
						if (counts.get(n.trim()) == null) {
							counts.put(n.trim(), 1);
						} else {
							counts.put(n.trim(), counts.get(n.trim()) + 1);
						}
					}
				}
			}
			List<MappingDisease> mappings = mappingDiseaseRepository.findAll();
			for (MappingDisease mapping: mappings) {
				Integer count = counts.get(mapping.getName());
				if (count != null) {
					mapping.setCount(count);
					mappingDiseaseRepository.save(mapping);
				}
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
	
	public void findRecordsWithMultiples () {
		List<String> multi = new ArrayList<>();
		List<Biological> records = bioRepository.findAll();
		for (Biological rec: records) {
			String disease = rec.getDisease();
			String tissue = rec.getTissue();
			String organ = rec.getOrgan();
			boolean multipleDisease = false;
			boolean multipleTissue = false;
			boolean multipleOrgan = false;
			
			if (disease != null && disease.contains(",")) {
				multipleDisease = true;
			}
			
			if (tissue != null && tissue.contains(",")) {
				multipleTissue = true;
			}
			
			if (organ != null && organ.contains(",")) {
				multipleOrgan = true;
			}
			
			if ((multipleDisease && multipleTissue) || 
					(multipleTissue && multipleOrgan) ||
					(multipleDisease && multipleOrgan)) {
				multi.add(rec.getCarb_id());
			}
		}
		
		logger.info ("Records with multiple multi values: " + multi.toString());
	}
	
	public void addPMIDs () {
		// add pmids
		PubmedUtil util = new PubmedUtil(apiKey);
		CrossRefAPI util2 = new CrossRefAPI();
		// go through existing ones and assign pmid if not assigned
		List<Publication> allPublications = publicationRepository.findAll();
		for (Publication pub: allPublications) {
			if ((pub.getPmid() == null || pub.getPmid().isEmpty())  && (pub.getDoiId() == null || pub.getDoiId().isEmpty())) {
				// check Pubmed to see if we can get the pmid
				try {
					if (pub.getChecked() == null || !pub.getChecked()) {
						List<Publication> matches = util.getPublicationByTitle(pub.getTitle());
						pub.setMatchCount(matches.size()+"");
						if (!matches.isEmpty()) {
							for (Publication m: matches) {
								if (pub.equals(m)) {
									pub.setPmid(m.getPmid());
									pub.setDoiId(m.getDoiId());
									break;
								} 
							}
							if (pub.getPmid() == null) {
								// check if we have pmid in journalid column
								if (pub.getJournalIdType() != null && pub.getJournalIdType().equalsIgnoreCase("PubMed")) {
									if (pub.getJournalId() != null) {
										pub.setPmid(pub.getJournalId());
										pub.setMatchDetails("Used journalid value as pmid");
										pub.setChecked(true);
									}
								}
								if (matches.size() == 1 && (pub.getChecked() == null || !pub.getChecked())) {
									Publication m = matches.get(0);
									if (m.getTitle() != null && m.getTitle().equalsIgnoreCase(pub.getTitle())) {
										pub.setMatchDetails("Single Match: " + m.getPmid() + "; Title matched, ");
										if (m.authorMatch(pub.getAuthor())) {
											pub.setMatchDetails(pub.getMatchDetails() + "Authors matched, ");
										} else {
											pub.setMatchDetails(pub.getMatchDetails() + " Authors did not match: " + m.getAuthor() + ",");
										}
										if (m.journalMatch(pub)) {
											pub.setMatchDetails(pub.getMatchDetails() + "Journal matched, ");
										}
										else {
											pub.setMatchDetails(pub.getMatchDetails() + " Journal did not match: " + 
													m.getJournalName() + " (" + m.getYear() + ") " + m.getVolume() + ": " + m.getPageRange() + ", ");
										}
										pub.setMatchDetails(pub.getMatchDetails().trim());
									}
								} else {
									pub.setMatchDetails("Multiple results from PubMed. None matched");
								}
							} 
						} else {
							// check if we have pmid in journalid column
							if (pub.getJournalIdType() != null && pub.getJournalIdType().equalsIgnoreCase("PubMed")) {
								if (pub.getJournalId() != null) {
									pub.setPmid(pub.getJournalId());
									pub.setMatchDetails("Used journalid value as pmid");
									pub.setChecked(true);
								}
							}
							if (pub.getPmid() == null) {
								// check crossRef to find matches
								List<Publication> crossRefMatches = util2.getPublicationByTitle(pub.getTitle());
								pub.setMatchCount(crossRefMatches.size()+"");
								if (!crossRefMatches.isEmpty()) {
									for (Publication m: crossRefMatches) {
										if (pub.equals(m)) {
											pub.setPmid(m.getPmid());
											pub.setDoiId(m.getDoiId());
											break;
										}
									}
								}
								if (pub.getDoiId() == null) {
									if (crossRefMatches.size() == 1) {
										Publication m = crossRefMatches.get(0);
										if (m.getTitle() != null && m.getTitle().equalsIgnoreCase(pub.getTitle())) {
											pub.setMatchDetails("Single CrossRef Match: " + m.getDoiId() + "; Title matched, ");
											if (m.authorMatch(pub.getAuthor())) {
												pub.setMatchDetails(pub.getMatchDetails() + "Authors matched, ");
											} else {
												pub.setMatchDetails(pub.getMatchDetails() + " Authors did not match: " + m.getAuthor() + ",");
											}
											if (m.journalMatch(pub)) {
												pub.setMatchDetails(pub.getMatchDetails() + "Journal matched, ");
											} else {
												pub.setMatchDetails(pub.getMatchDetails() + " Journal did not match: " + 
														m.getJournalName() + " (" + m.getYear() + ") " + m.getVolume() + ": " + m.getPageRange() + ", ");
											}
											pub.setMatchDetails(pub.getMatchDetails().trim());
										}
									} else {
										pub.setMatchDetails("Multiple results from CrossRef. None matched");
									}
								}
							}
						}
						pub.setChecked(true);
						publicationRepository.save(pub);
						
						try {
					        Thread.sleep(100); // wait 100 milliseconds between requests
					    } catch (InterruptedException e) {
					        Thread.currentThread().interrupt(); // restore interrupted status
					    }
					}
				} catch (Exception e) {
					logger.error("Error getting publication from PubMed", e);
				}
			}
		}
	}

	public void generateExcelFiles () {
		
		// Tissue mappings
		List<String[]> rows = new ArrayList<>();
		List<MappingTissue> allRows = mappingTissueRepository.findAll();
		Map<Long, List<String>> recordMap = new HashMap<>();
		String[] header = {"ID", "count", "name", "namespacename", "namespaceid", "mappingname", "rank", "matchCount"};
		rows.add(header);
		for (MappingTissue m: allRows) {
			if (m.getNamespaceName() == null) {
				String[] row = new String[8];
				row[0] = m.getId()+"";
				row[1] = m.getCount()+ "";
				row[2] = m.getName();
				row[7] = m.getMatchCount() + "";
				// find records with this value
				List<Biological> bsList = bioRepository.findByTissueIgnoreCase(m.getName());
				Set<String> recordList = new HashSet<>();
				for (Biological bs: bsList) {
					String cc = bs.getCarb_id();
					recordList.add(cc);
				}
				recordMap.put(m.getId(), new ArrayList<>(recordList));
				rows.add(row);
			}
		}
		List<String[]> recordRows = new ArrayList<>();
		String[] rHeader = {"ID", "CC Number"};
		recordRows.add(rHeader);
		for (Long id: recordMap.keySet()) {
			List<String> records = recordMap.get(id);
			for (String cc: records) {
				String[] row = new String[2];
				row[0] = id+"";
				row[1] = cc;
				recordRows.add(row);
			}
		}
		try {
			writeToExcel (rows, "Mappings", "mapping_Tissue.xlsx", recordRows, "records");
		} catch (IOException e1) {
			logger.error("Error generating Excel file for Tissues", e1);
		}
		
		// Scientific Name Mappings
		recordMap = new HashMap<>();
		rows = new ArrayList<>();
		List<MappingScientificName> allSpecies = mappingSpeciesRepository.findAll();
		rows.add(header);
		for (MappingScientificName m: allSpecies) {
			if (m.getNamespaceName() == null) {
				String[] row = new String[8];
				row[0] = m.getId()+"";
				row[1] = m.getCount()+ "";
				row[2] = m.getName();
				row[7] = m.getMatchCount() + "";
				// find records with this value
				List<Biological> bsList = bioRepository.findByScientificnameIgnoreCase(m.getName());
				Set<String> recordList = new HashSet<>();
				for (Biological bs: bsList) {
					String cc = bs.getCarb_id();
					recordList.add(cc);
				}
				recordMap.put(m.getId(), new ArrayList<>(recordList));
				rows.add(row);
			}
		}
		recordRows = new ArrayList<>();
		recordRows.add(rHeader);
		for (Long id: recordMap.keySet()) {
			List<String> records = recordMap.get(id);
			for (String cc: records) {
				String[] row = new String[2];
				row[0] = id+"";
				row[1] = cc;
				recordRows.add(row);
			}
		}
		try {
			writeToExcel (rows, "Mappings", "mapping_ScientificName.xlsx", recordRows, "records");
		} catch (IOException e1) {
			logger.error("Error generating Excel file for Species", e1);
		}
		
		// Disease Name Mappings
		recordMap = new HashMap<>();
		rows = new ArrayList<>();
		List<MappingDisease> allDisease = mappingDiseaseRepository.findAll();
		rows.add(header);
		for (MappingDisease m: allDisease) {
			if (m.getNamespaceName() == null) {
				String[] row = new String[8];
				row[0] = m.getId()+"";
				row[1] = m.getCount()+ "";
				row[2] = m.getName();
				row[7] = m.getMatchCount() + "";
				// find records with this value
				List<Biological> bsList = bioRepository.findByDiseaseIgnoreCase(m.getName());
				Set<String> recordList = new HashSet<>();
				for (Biological bs: bsList) {
					String cc = bs.getCarb_id();
					recordList.add(cc);
				}
				recordMap.put(m.getId(), new ArrayList<>(recordList));
				rows.add(row);
			}
		}
		recordRows = new ArrayList<>();
		recordRows.add(rHeader);
		for (Long id: recordMap.keySet()) {
			List<String> records = recordMap.get(id);
			for (String cc: records) {
				String[] row = new String[2];
				row[0] = id+"";
				row[1] = cc;
				recordRows.add(row);
			}
		}
		try {
			writeToExcel (rows, "Mappings", "mapping_Disease.xlsx", recordRows, "records");
		} catch (IOException e1) {
			logger.error("Error generating Excel file for Diseases", e1);
		}
			
			
		// Organ Mappings
		recordMap = new HashMap<>();
		rows = new ArrayList<>();
		List<MappingOrgan> allOrgans = mappingOrganRepository.findAll();
		rows.add(header);
		for (MappingOrgan m: allOrgans) {
			if (m.getNamespaceName() == null) {
				String[] row = new String[8];
				row[0] = m.getId()+"";
				row[1] = m.getCount()+ "";
				row[2] = m.getName();
				row[7] = m.getMatchCount() + "";
				// find records with this value
				List<Biological> bsList = bioRepository.findByOrganIgnoreCase(m.getName());
				Set<String> recordList = new HashSet<>();
				for (Biological bs: bsList) {
					String cc = bs.getCarb_id();
					recordList.add(cc);
				}
				recordMap.put(m.getId(), new ArrayList<>(recordList));
				rows.add(row);
			}
		}
		recordRows = new ArrayList<>();
		recordRows.add(rHeader);
		for (Long id: recordMap.keySet()) {
			List<String> records = recordMap.get(id);
			for (String cc: records) {
				String[] row = new String[2];
				row[0] = id+"";
				row[1] = cc;
				recordRows.add(row);
			}
		}
		try {
			writeToExcel (rows, "Mappings", "mapping_Organ.xlsx", recordRows, "records");
		} catch (IOException e1) {
			logger.error("Error generating Excel file for Organs", e1);
		}
		
		// publications
		rows = new ArrayList<>();
		List<Publication> allPublications = publicationRepository.findAll();
		//Map<Long, List<String>> recordMap = new HashMap<>();
		String[] header2 = {"ID", "Title", "Authors", "Journal", "PMID", "DOI", "Match Details", "Journal Key", "Journal ID", "Journal ID Type"};
		rows.add(header2);
		for (Publication m: allPublications) {
			if (m.getPmid() == null && m.getDoiId() == null) {
				String[] row = new String[10];
				row[0] = m.getId() +"";
				row[1] = m.getTitle();
				row[2] = m.getAuthor();
				row[3] = m.getJournalName() + " (" + m.getYear() + ") " + m.getVolume() + ": " + m.getPageRange();
				row[6] = m.getMatchDetails();
				row[7] = m.getJournalKey();
				row[8] = m.getJournalId();
				row[9] = m.getJournalIdType();
				rows.add(row);
			}
		}
		
		try {
			writeToExcel (rows, "Publications", "cfg_Publication.xlsx", null, null);
		} catch (IOException e1) {
			logger.error("Error generating Excel file for Publications", e1);
		}
		
	}
	
	public static void writeToExcel (List<String[]> rows, String sheetName, String filename, List<String[]> records, String sheetName2) throws IOException {
		FileOutputStream excelWriter = new FileOutputStream(filename);
		Workbook workbook = new XSSFWorkbook();
		
        XSSFFont font= (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setItalic(false);
        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(font);
        
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle wrapTextStyle = workbook.createCellStyle();
        wrapTextStyle.setWrapText(true);
        
        CreationHelper createHelper = workbook.getCreationHelper();
        

        CellStyle hlinkStyle = workbook.createCellStyle();
        XSSFFont hlinkFont= (XSSFFont) workbook.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        
        // first row is the header row
        if (rows.size() > 0) {
        	String[] headerRow = rows.get(0);
        	Row header = sheet.createRow(0);
        	int i=0;
        	for (String col: headerRow) {
        		Cell cell = header.createCell(i++);
        		cell.setCellValue(col);
        		cell.setCellStyle(boldStyle);
        	}
        	
        	for (i=1; i< rows.size(); i++) {
        		String[] row = rows.get(i);
        		Row entry = sheet.createRow(i);
        		int j=0;
        		for (String col: row) {
        			Cell cell = entry.createCell(j++);
        			cell.setCellStyle(wrapTextStyle);
    				cell.setCellValue(col);
           		}
        	}
        }
        
        if (records != null && !records.isEmpty()) {
        	Sheet recordsSheet = workbook.createSheet(sheetName2);
        	String[] headerRow = records.get(0);
        	Row header = recordsSheet.createRow(0);
        	int i=0;
        	for (String col: headerRow) {
        		Cell cell = header.createCell(i++);
        		cell.setCellValue(col);
        		cell.setCellStyle(boldStyle);
        	}
        	
        	for (i=1; i< records.size(); i++) {
        		String[] row = records.get(i);
        		Row entry = recordsSheet.createRow(i);
        		int j=0;
        		for (String col: row) {
        			Cell cell = entry.createCell(j++);
    				cell.setCellValue(col);
        		}
        	}
        	
        }
        
        workbook.write(excelWriter);
        excelWriter.close();
        workbook.close();
	}
}
