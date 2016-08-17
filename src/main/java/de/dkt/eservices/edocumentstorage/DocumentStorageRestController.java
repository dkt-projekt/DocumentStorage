package de.dkt.eservices.edocumentstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.dkt.eservices.edocumentstorage.service.DocumentCollectionService;
import de.dkt.eservices.edocumentstorage.service.DocumentService;
import de.dkt.eservices.edocumentstorage.service.DocumentProcessorService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import eu.freme.common.rest.BaseRestController;
/**
 * Rest Controller for /document-storage/{collectionName} endpoints
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
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
	
	@Autowired
	DocumentProcessorService documentProcessorService;

	Logger logger = Logger.getLogger(DocumentStorageRestController.class);
	

	/**
	 * Upload either single document to a collection or a zip file.
	 * 
	 * @param name
	 * @param request
	 * @param contentTypeHeader
	 * @param collectionName
	 * @return
	 */
	@RequestMapping(value = "/document-storage/{collectionName}", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFileHandler(
			@RequestParam("fileName") String name,
			HttpServletRequest request,
			@RequestHeader("Content-Type") String contentTypeHeader,
			@PathVariable String collectionName) {

		DocumentCollection dc = documentCollectionRepository
				.findOneByName(collectionName);
		if (dc == null) {
			dc = documentCollectionService.createCollection(collectionName);
		}

		if (contentTypeHeader != null
				&& contentTypeHeader.toLowerCase().equals("application/zip")) {
			// zip archive upload

			// move zip archive to temp
			File tempFile = null;
			try {
				tempFile = File.createTempFile("dkt-upload", "zip");
				FileOutputStream fos = new FileOutputStream(tempFile);
				IOUtils.copy(request.getInputStream(), fos);
				fos.close();

				documentCollectionService.addZipFileToCollection(dc, tempFile);
			} catch (Exception e) {
				logger.error("unzip failed", e);
				throw new BadRequestException("failed to read zip archive");
			} finally {
				if (tempFile != null) {
					tempFile.delete();
				}
			}

		} else {
			// single file upload
			try {
				documentService.addFileToCollection(request.getInputStream(),
						name, dc);
			} catch (IOException e) {
				logger.error("file upload failed", e);
				throw new InternalServerErrorException("file upload failed");
			}
		}
		
		// wake up pipeline processor
		documentProcessorService.wakeupWorkers();

		ResponseEntity<String> response = new ResponseEntity<String>(
				"File uploaded successful", HttpStatus.OK);
		return response;
	}

	/**
	 * Return all documents within a collection.
	 * 
	 * @param collectionName
	 * @return
	 */
	@RequestMapping(value = "/document-storage/{collectionName}", method = RequestMethod.GET)
	public Collection<Document> getFiles(@PathVariable String collectionName) {

		DocumentCollection dc = documentCollectionRepository
				.findOne(collectionName);

		if (dc == null) {
			throw new NotFoundException(
					"Cannot find document collection with name \""
							+ collectionName + "\"");
		}

		return documentRepository.findAllByCollection(dc);
	}
}
