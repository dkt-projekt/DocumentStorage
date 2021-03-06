package de.dkt.eservices.edocumentstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.dkt.eservices.edocumentstorage.exception.DocumentCollectionDoesNotExistException;
import de.dkt.eservices.edocumentstorage.exception.DocumentCollectionExistsException;
import de.dkt.eservices.edocumentstorage.service.DocumentCollectionService;
import de.dkt.eservices.edocumentstorage.service.DocumentService;
import de.dkt.eservices.edocumentstorage.service.DocumentProcessorService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.persistence.dao.DocumentDAO;
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
	
	@Autowired
	DocumentDAO documentDao;

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
	@RequestMapping(value = "/document-storage/collections/{collectionName}/documents", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFileHandler(
			@RequestParam("fileName") String name, HttpServletRequest request,
			@RequestHeader("Content-Type") String contentTypeHeader,
			@PathVariable String collectionName,
			@RequestParam(value="pipeline", required=true) String pipeline ) {

		DocumentCollection dc = documentCollectionRepository
				.findOneByName(collectionName);
		if (dc == null) {
			throw new DocumentCollectionDoesNotExistException();
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

				documentCollectionService.addZipFileToCollection(dc, tempFile, pipeline);
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
						name, dc, pipeline);
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
	
	@RequestMapping(value = "/document-storage/collections/{collectionName}", method = RequestMethod.POST)
	public String createCollection(
			@PathVariable String collectionName) {

		DocumentCollection dc = documentCollectionRepository
				.findOneByName(collectionName);
		if (dc == null) {
			dc = documentCollectionService.createCollection(collectionName);
			return "created collection \"" + collectionName + "\"";
		} else{
			throw new DocumentCollectionExistsException();
		}
	}

	/**
	 * Return all documents within a collection.
	 * 
	 * @param collectionName
	 * @return
	 */
	@RequestMapping(value = "/document-storage/collections/{collectionName}/documents", method = RequestMethod.GET)
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

	/**
	 * Return the processing status
	 * 
	 * @param collectionName
	 * @return
	 */
	@RequestMapping(value = "/document-storage/collections/{collectionName}/status", method = RequestMethod.GET)
	public String getStatus(@PathVariable String collectionName) {

		DocumentCollection dc = documentCollectionRepository
				.findOne(collectionName);

		if (dc == null) {
			throw new NotFoundException(
					"Cannot find document collection with name \""
							+ collectionName + "\"");
		}
		
		HashMap<Document.Status,Integer> counts = documentDao.getDocumentsStatus(dc);
		
		JSONObject json = new JSONObject();
		json.put("counts", new JSONObject(counts));

		boolean finished = counts.get(Document.Status.NOT_PROCESSED) == 0
				&& counts.get(Document.Status.CURRENTLY_PROCESSING) == 0;
		json.put("finished", finished);

		return json.toString();
	}
	
	/**
	 * Return all collections (only names).
	 * 
	 * @param collectionName
	 */
	@RequestMapping(value = "/document-storage/collections/{collectionName}", method = RequestMethod.DELETE)
	public void deleteCollection(@PathVariable String collectionName) {
		
		DocumentCollection dc = documentCollectionRepository
				.findOne(collectionName);
		
		if( dc == null ){
			throw new DocumentCollectionDoesNotExistException();
		}
		
		try {
			documentCollectionService.deleteCollection(dc);
		} catch (IOException e) {
			logger.error(e);
			throw new InternalServerErrorException("failed to delete document collection");
		}
	}
	

	@RequestMapping(value = "/document-storage/collections", method = RequestMethod.GET)
	public String getCollections() {
		
		Iterator<DocumentCollection> itr = documentCollectionRepository.findAll().iterator();
		ArrayList<String> list = new ArrayList<String>();
		while( itr.hasNext() ){
			list.add(itr.next().getName());
		}
		JSONArray json = new JSONArray(list);
		return json.toString();
		
	}
}
