package de.dkt.eservices.edocumentstorage;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.dkt.eservices.edocumentstorage.service.DocumentCollectionService;
import de.dkt.eservices.edocumentstorage.service.DocumentService;

@Configuration
@ComponentScan
public class DocumentStorageConfig {

	// configurable directory to store all documents
	String documentStorageLocationStr = "documents/";
	
	// file that points to documentStorageLocationStr
	File documentStorageLocation;
	
	@PostConstruct
	public void postConstruct(){
		
		// create storage dir if it does not exist
		documentStorageLocation = new File(documentStorageLocationStr);
		if( documentStorageLocation.exists() && documentStorageLocation.isFile() ){
			throw new RuntimeException(String.format("Cannot create storage directory \"%s\"", documentStorageLocation));
		}
		
		if( !documentStorageLocation.exists() && !documentStorageLocation.mkdirs()){
			throw new RuntimeException(String.format("Cannot create storage directory \"%s\"", documentStorageLocation)); 
		}		
	}
	
	public File getDocumentStorageLocation(){
		return documentStorageLocation;
	}
	
	@Bean
	public DocumentCollectionService getDocumentCollectionService(){
		return new DocumentCollectionService();
	}
	
	@Bean
	public DocumentService getFileService(){
		return new DocumentService();
	}

	public void setDocumentStorageLocationStr(String documentStorageLocationStr) {
		this.documentStorageLocationStr = documentStorageLocationStr;
	}
	
	
}