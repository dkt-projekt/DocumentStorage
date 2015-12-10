package eu.freme.broker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.freme.broker.edocumentstorage.api.EDocumentStorageService;


//@SpringBootApplication
//@ComponentScan("de.dkt.eservices.eopennlp.api")
@Configuration
public class EDocumentStorageConfig {
	
	@Bean
	public EDocumentStorageService getEntityApi(){
		return new EDocumentStorageService();
	}
	
}
