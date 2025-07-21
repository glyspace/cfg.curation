package org.glygen.cfgcuration.service;

import java.util.List;

import org.glygen.cfgcuration.dao.StructureRepository;
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
	
	final StructureRepository structureRepository;
	
	public CFGToTablemakerService(StructureRepository structureRepository) {
		this.structureRepository = structureRepository;
	}
	
	public void createGlycans () {
		this.tablemaker = TableMakerAPI.getInstance();
		this.tablemaker.setApiURL(apiUrl);
		this.tablemaker.setUserName(userId);
		this.tablemaker.setPassword(password);
		List<Structures> structures = structureRepository.findAll();
		for (Structures str: structures) {
			try {
				String seq = str.getLinearcode();
				if (seq.contains("#")) {   // get rid of linker at the end
					seq = seq.substring(0, seq.indexOf("#"));
				}
				if (seq.contains(";")) {   // get rid of linker at the end
					seq = seq.substring(0, seq.indexOf(";"));
				}
				this.tablemaker.addGlycan(seq);
			} catch (Exception e) {
				logger.error("could not convert sequence to GlycoCT: " + str.getLinearcode(), e);
			}
		}
		
	}

}
