package org.glygen.cfgcuration;

import org.glygen.cfgcuration.service.CFGCurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment;

@SpringBootApplication
public class CFGCurationApplication {
	
	@Autowired
	CFGCurationService service;

	public static void main(String[] args) {
		new SpringApplicationBuilder()
	    .environment(new StandardEncryptableEnvironment())
	    .sources(CFGCurationApplication.class).run(args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup(ApplicationReadyEvent event) {
		NamespaceHandler.loadNamespaces();
		
		//service.assignCarbKeys();
		service.createPublications();
		service.createMappingTables();
		service.addInformationToMappingTables();
	}
}
