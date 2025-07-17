package org.glygen.cfgcuration.service;

import java.util.List;

import org.glygen.cfgcuration.dao.StructureRepository;
import org.glygen.cfgcuration.model.Structures;
import org.glygen.cfgcuration.util.SequenceUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class CFGToTablemakerService {
	
	static Logger logger = org.slf4j.LoggerFactory.getLogger(CFGCurationService.class);
	
	final StructureRepository structureRepository;
	
	public CFGToTablemakerService(StructureRepository structureRepository) {
		this.structureRepository = structureRepository;
	}
	
	public void createGlycans () {
		List<Structures> structures = structureRepository.findAll();
		for (Structures str: structures) {
			try {
				String glycoCT = SequenceUtils.parseIUPACSequence(str.getIupac());
				
			} catch (Exception e) {
				logger.error("could not convert sequence to GlycoCT: " + str.getIupac(), e);
			}
		}
		
	}

}
