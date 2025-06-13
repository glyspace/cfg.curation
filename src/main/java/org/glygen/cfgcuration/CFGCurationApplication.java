package org.glygen.cfgcuration;

import java.util.ArrayList;
import java.util.List;

import org.glygen.cfgcuration.model.mapping.Mapping;
import org.glygen.cfgcuration.service.CFGCurationService;
import org.glygen.cfgcuration.util.ComparisonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment;

@SpringBootApplication
public class CFGCurationApplication {
	
	@Autowired
	CFGCurationService service;
	
	@Autowired
	ComparisonUtil comparisonService;


	public static void main(String[] args) {
		new SpringApplicationBuilder()
	    .environment(new StandardEncryptableEnvironment())
	    .sources(CFGCurationApplication.class).run(args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup(ApplicationReadyEvent event) {
		ApplicationArguments args = event.getApplicationContext().getBean(ApplicationArguments.class);
		NamespaceHandler.loadNamespaces();
		
		if (args.containsOption("compare")) {
			List<String> tablenames = args.getOptionValues("compare");
			List<String> filenames = args.getOptionValues("file");
			List<Mapping> mappings = new ComparisonUtil().compareFiles(filenames, tablenames.get(0));
			//service.updateMappings(mappings, tablenames.get(0));
		} else {
			//service.assignCarbKeys();
			//service.findRecordsWithMultiples();
			//service.createPublications();
			//service.createMappingTables();
			//service.addInformationToMappingTables();
			service.addPMIDs();
			service.generateExcelFiles();
		}
		
	}
}
