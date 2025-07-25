package org.glygen.cfgcuration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.glygen.cfgcuration.dao.BiologicalRepository;
import org.glygen.cfgcuration.dao.MappingDiseaseRepository;
import org.glygen.cfgcuration.dao.MappingOrganRepository;
import org.glygen.cfgcuration.dao.MappingSpeciesRepository;
import org.glygen.cfgcuration.dao.MappingTissueRepository;
import org.glygen.cfgcuration.dao.PublicationRepository;
import org.glygen.cfgcuration.dao.StructureRepository;
import org.glygen.cfgcuration.dao2.RemoteStructureRepository;
import org.glygen.cfgcuration.glycomedb.GlycoCTStructure;
import org.glygen.cfgcuration.glycomedb.RemoteStructure;
import org.glygen.cfgcuration.model.Biological;
import org.glygen.cfgcuration.model.Publication;
import org.glygen.cfgcuration.model.Structures;
import org.glygen.cfgcuration.model.mapping.MappingDisease;
import org.glygen.cfgcuration.model.mapping.MappingScientificName;
import org.glygen.cfgcuration.model.mapping.MappingTissue;
import org.glygen.cfgcuration.model.tablemaker.CollectionView;
import org.glygen.cfgcuration.model.tablemaker.DatasetInputView;
import org.glygen.cfgcuration.model.tablemaker.Datatype;
import org.glygen.cfgcuration.model.tablemaker.Glycan;
import org.glygen.cfgcuration.model.tablemaker.License;
import org.glygen.cfgcuration.model.tablemaker.Metadata;
import org.glygen.cfgcuration.util.TableMakerAPI;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CFGToTablemakerService {
	
	@Value("${tablemaker.api-url}")
    String apiUrl;
    
	@Value("${tablemaker.password}")
    String password;
    
    @Value("${tablemaker.user-id}")
    String userId;
    
    TableMakerAPI tablemaker;
	
	static Logger logger = org.slf4j.LoggerFactory.getLogger(CFGCurationService.class);
	
	Map<String, String>  glycanGlytoucanMap = new HashMap<>();
	
	final StructureRepository structureRepository;
	final BiologicalRepository biologicalRepository;
	final PublicationRepository publicationRepository;
	final RemoteStructureRepository remoteRepository;
	final MappingDiseaseRepository mappingDiseaseRepository;
	private final MappingTissueRepository mappingTissueRepository;
	private final MappingSpeciesRepository mappingSpeciesRepository;
	private final MappingOrganRepository mappingOrganRepository;
	
	public CFGToTablemakerService(StructureRepository structureRepository, PublicationRepository publicationRepository, BiologicalRepository biologicalRepository, RemoteStructureRepository remoteRepository, MappingTissueRepository mappingTissueRepository, MappingSpeciesRepository mappingSpeciesRepository, MappingOrganRepository mappingOrganRepository, MappingDiseaseRepository mappingDiseaseRepository) {
		this.structureRepository = structureRepository;
		this.biologicalRepository = biologicalRepository;
		this.publicationRepository = publicationRepository;
		this.remoteRepository = remoteRepository;
		this.mappingDiseaseRepository = mappingDiseaseRepository;
		this.mappingTissueRepository = mappingTissueRepository;
		this.mappingSpeciesRepository = mappingSpeciesRepository;
		this.mappingOrganRepository = mappingOrganRepository;
	}
	
	public void createGlycans () {
		this.tablemaker = TableMakerAPI.getInstance();
		this.tablemaker.setApiURL(apiUrl);
		this.tablemaker.setUserName(userId);
		this.tablemaker.setPassword(password);
		List<Structures> structures = structureRepository.findAll();
		for (Structures str: structures) {
			String carbId = str.getCarb_id();

			Optional<RemoteStructure>  handle = remoteRepository.findByResourceIdWithStructures(carbId);
			if (handle.isPresent() && !handle.get().getStructures().isEmpty()) {
				GlycoCTStructure structure = handle.get().getStructures().iterator().next();
				try {
					String glytoucanId = this.tablemaker.addGlycanGlycoCT(structure.getGlycoCT());
					if (glytoucanId != null) {
						glycanGlytoucanMap.put(str.getCarb_id(), glytoucanId);
					}
				} catch (Exception e) {
					logger.error("could not add glycan with glycoCT: " + structure.getGlycoCT(), e);
				}
			} else {
				String seq = str.getLinearcode();
				if (seq == null) {
					logger.info("no linearcode for structure: " + str.getCarb_id());
					continue;
				}
				try {
					if (seq.contains("#")) {   // get rid of linker at the end
						seq = seq.substring(0, seq.indexOf("#"));
					}
					if (seq.contains(";")) {   // get rid of linker at the end
						seq = seq.substring(0, seq.indexOf(";"));
					}
					String glytoucanId = this.tablemaker.addGlycan(seq);
					if (glytoucanId != null) {
						glycanGlytoucanMap.put(str.getCarb_id(), glytoucanId);
					}
				} catch (Exception e) {
					// catch DuplicateException
					
					logger.error("could not convert sequence to GlycoCT: " + seq, e);
				}
			}
		}
	}
	
	public void createCollectionsAndPublishDataset () {
		this.tablemaker = TableMakerAPI.getInstance();
		this.tablemaker.setApiURL(apiUrl);
		this.tablemaker.setUserName(userId);
		this.tablemaker.setPassword(password);
		List<Structures> structures = structureRepository.findAll();
		List<CollectionView> addedCollections = new ArrayList<CollectionView>();
		StringBuffer notes = new StringBuffer();
		for (Structures str: structures) {
			try {
				String glytoucanId = glycanGlytoucanMap.get(str.getCarb_id());
				if (glytoucanId == null) {
					logger.error("Could not get glytoucanId for " + str.getCarb_id());
					continue;
				}
				if (str.getCarb_key() != null) {
					List<Publication> pubs = publicationRepository.findByCarbKey(str.getCarb_key());
					for (Publication pub: pubs) {
						if (pub.getPmid() == null && pub.getDoiId() == null) {
							logger.info("There is no pmid for this publication " + pub.getId());
							notes.append("Not including " + pub.getCarbKey() + " since there is no pmid or doi\n");
							continue;
						}
						// create a collection for each glycan-paper pair
						CollectionView collection = new CollectionView();
						collection.setGlycans(new ArrayList<>());
						Glycan glycan = new Glycan();
						glycan.setGlytoucanID(glytoucanId);
						collection.getGlycans().add(glycan);
						Metadata metadata = new Metadata();
						if (pub.getPmid() != null) metadata.setValue(pub.getPmid());
						else metadata.setValue(pub.getDoiId());
						Datatype datatype = new Datatype();
						datatype.setDatatypeId(2L);
						metadata.setType(datatype);
						collection.setMetadata(new ArrayList<Metadata>());
						collection.getMetadata().add(metadata);
						
						String id = this.tablemaker.addCollection (collection);
						collection.setCollectionId(Long.parseLong(id));
						addedCollections.add(collection);
					}
					
					// add a collection for each biological entry
					List<Biological> bList = biologicalRepository.findByCarbId(str.getCarb_id());
					for (Biological bio: bList) {
						CollectionView collection = new CollectionView();
						collection.setGlycans(new ArrayList<>());
						Glycan glycan = new Glycan();
						glycan.setGlytoucanID(glytoucanId);
						collection.getGlycans().add(glycan);
						collection.setMetadata(new ArrayList<Metadata>());
						if (bio.getScientificname() != null) {
							// find the mapping
							Optional<MappingScientificName> mapping = mappingSpeciesRepository.findByNameIgnoreCase(bio.getScientificname());
							if (mapping.isPresent()) {
								String namespaceId = mapping.get().getNamespaceId();
								if (namespaceId != null) {
									Metadata metadata = new Metadata();
									Datatype datatype = new Datatype();
									datatype.setDatatypeId(3L);
									metadata.setType(datatype);
									metadata.setValueId(namespaceId);
									metadata.setValue(mapping.get().getNamespaceName());
									collection.getMetadata().add(metadata);
								}
							}
						}
						if (bio.getDisease() != null) {
							// find the mapping
							Optional<MappingDisease> mapping = mappingDiseaseRepository.findByNameIgnoreCase(bio.getDisease());
							if (mapping.isPresent()) {
								String namespaceId = mapping.get().getNamespaceId();
								if (namespaceId != null) {
									Metadata metadata = new Metadata();
									Datatype datatype = new Datatype();
									datatype.setDatatypeId(7L);
									metadata.setType(datatype);
									metadata.setValueId(namespaceId);
									metadata.setValue(mapping.get().getNamespaceName());
									collection.getMetadata().add(metadata);
								}
							}
						}
						if (bio.getTissue() != null) {
							// find the mapping
							Optional<MappingTissue> mapping = mappingTissueRepository.findByNameIgnoreCase(bio.getTissue());
							if (mapping.isPresent()) {
								String namespaceId = mapping.get().getNamespaceId();
								if (namespaceId != null) {
									Metadata metadata = new Metadata();
									Datatype datatype = new Datatype();
									datatype.setDatatypeId(5L);
									metadata.setType(datatype);
									metadata.setValueId(namespaceId);
									metadata.setValue(mapping.get().getNamespaceName());
									collection.getMetadata().add(metadata);
								}
							}
						}
						//TODO handle organs
						if (collection.getMetadata().isEmpty()) {
							logger.info ("No metadata for " + str.getCarb_id());
							notes.append("Not including " +  str.getCarb_key() + " since there is no metadata");
						} else {
							//TODO fix contributor information
							Metadata metadata = new Metadata();
							Datatype datatype = new Datatype();
							datatype.setDatatypeId(30L);
							metadata.setType(datatype);
							metadata.setValue("curatedBy:Sena Arpinar (sena@uga.edu, University of Georgia)|createdWith:GlyTableMaker (https://glygen.ccrc.uga.edu/tablemaker)");
							collection.getMetadata().add(metadata);
							String id = this.tablemaker.addCollection (collection);
							collection.setCollectionId(Long.parseLong(id));
							addedCollections.add(collection);
						}

					}
				}
				
			} catch (Exception e) {
				logger.error("could not convert sequence to GlycoCT: " + str.getLinearcode(), e);
			}
		}
		
		if (!addedCollections.isEmpty()) {
			DatasetInputView dataset = new DatasetInputView();
			dataset.setName("Consortium for Functional Glycomics (CFG) Database");
			dataset.setLicense(new License());
			dataset.getLicense().setId(2L);
			dataset.getLicense().setName("CC BY 4.0");
			dataset.getLicense().setUrl("https://creativecommons.org/licenses/by/4.0/");
			dataset.getLicense().setCommercialUse(true);
			dataset.getLicense().setAttribution("You must give appropriate credit , provide a link to the license, and indicate if changes were made . You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.");
			dataset.getLicense().setDistribution("No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.");
			dataset.setNotes(notes.toString());
			dataset.setCollections(addedCollections);
			
			this.tablemaker.publishDataset(dataset);
		}
		
	}

}
