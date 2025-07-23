package org.glygen.cfgcuration.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.glygen.cfgcuration.dao.BiologicalRepository;
import org.glygen.cfgcuration.dao.PublicationRepository;
import org.glygen.cfgcuration.dao.StructureRepository;
import org.glygen.cfgcuration.dao2.RemoteStructureRepository;
import org.glygen.cfgcuration.glycomedb.GlycoCTStructure;
import org.glygen.cfgcuration.glycomedb.RemoteStructure;
import org.glygen.cfgcuration.model.Publication;
import org.glygen.cfgcuration.model.Structures;
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
	
	public CFGToTablemakerService(StructureRepository structureRepository, PublicationRepository publicationRepository, BiologicalRepository biologicalRepository, RemoteStructureRepository remoteRepository) {
		this.structureRepository = structureRepository;
		this.biologicalRepository = biologicalRepository;
		this.publicationRepository = publicationRepository;
		this.remoteRepository = remoteRepository;
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
					// catch DuplicateException
					
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
	
	public void createCollections () {
		this.tablemaker = TableMakerAPI.getInstance();
		this.tablemaker.setApiURL(apiUrl);
		this.tablemaker.setUserName(userId);
		this.tablemaker.setPassword(password);
		List<Structures> structures = structureRepository.findAll();
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
						// create a collection for each glycan-paper pair
						
					}
				}
				
			} catch (Exception e) {
				logger.error("could not convert sequence to GlycoCT: " + str.getLinearcode(), e);
			}
		}
		
	}

}
