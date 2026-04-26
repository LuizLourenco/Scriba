package br.dev.lourenco.scriba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ScribaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScribaApplication.class, args);
	}

}
