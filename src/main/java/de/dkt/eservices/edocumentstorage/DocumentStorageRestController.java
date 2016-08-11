package de.dkt.eservices.edocumentstorage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.NotFoundException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.dkt.eservices.edocumentstorage.service.DocumentCollectionService;
import de.dkt.eservices.edocumentstorage.service.DocumentService;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import eu.freme.common.rest.BaseRestController;

@RestController
public class DocumentStorageRestController extends BaseRestController {

	@Autowired
	DocumentService documentService;

	@Autowired
	DocumentCollectionService documentCollectionService;

	@Autowired
	DocumentCollectionRepository documentCollectionRepository;
	
	@Autowired
	DocumentRepository documentRepository;

	Logger logger = Logger.getLogger(DocumentStorageRestController.class);

	@RequestMapping(value = "/document-storage/{collectionName}", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFileHandler(
			@RequestParam("fileName") String name,
			@RequestParam("file") MultipartFile file,
			@PathVariable String collectionName) {

		DocumentCollection dc = documentCollectionRepository
				.findOneByName(collectionName);
		if (dc == null) {
			dc = documentCollectionService.createCollection(collectionName);
		}

		try {
			documentService
					.addFileToCollection(file.getInputStream(), name, dc);
		} catch (IOException e) {
			logger.error("file upload failed", e);
			throw new InternalServerErrorException("file upload failed");
		}

		ResponseEntity<String> response = new ResponseEntity<String>(
				"File uploaded successful", HttpStatus.OK);
		return response;
	}

	@RequestMapping(value = "/document-storage/{collectionName}", method = RequestMethod.GET)
	public Collection<Document> getFiles(@PathVariable String collectionName) {
		
		DocumentCollection dc = documentCollectionRepository.findOne(collectionName);
		
		if( dc == null ){
			throw new NotFoundException("Cannot find document collection with name \"" + collectionName + "\"");
		}
		
		return documentRepository.findAllByCollection(dc);
	}
}
