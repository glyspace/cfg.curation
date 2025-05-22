package org.glygen.cfgcuration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment;

@SpringBootApplication
public class CFGCurationApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder()
	    .environment(new StandardEncryptableEnvironment())
	    .sources(CFGCurationApplication.class).run(args);
	}

}
