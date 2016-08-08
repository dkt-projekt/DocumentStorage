package de.dkt.eservices.edocumentstorage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import de.dkt.common.exceptions.LoggedExceptions;
import eu.freme.common.rest.BaseRestController;

@RestController
public class EDocumentStorageServiceStandAlone extends BaseRestController {
	
	Logger logger = Logger.getLogger(EDocumentStorageServiceStandAlone.class);

	@Autowired
	EDocumentStorageService service;

	@RequestMapping(value = "/e-documentstorage/testURL", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> testURL(
			@RequestParam(value = "preffix", required = false) String preffix,
            @RequestBody(required = false) String postBody) throws Exception {

    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.add("Content-Type", "text/plain");
    	ResponseEntity<String> response = new ResponseEntity<String>("The restcontroller is working properly", responseHeaders, HttpStatus.OK);
    	return response;
	}

	
//	@RequestMapping(value = "/e-documentstorage/listCollections", method = { RequestMethod.POST, RequestMethod.GET })
// 	public ResponseEntity<String> listCollections(
//			HttpServletRequest request, 
//			@RequestParam(value = "user", required = false) String user,
//			@RequestParam(value = "limit", required = false, defaultValue="0") int limit,
//			@RequestParam(value = "collectionId", required = false) String collectionId,
//			@RequestBody(required = false) String postBody) throws Exception {
//		try {
//			String result=null;
//			result = service.listCollections(user,limit);
//			HttpHeaders responseHeaders = new HttpHeaders();
//			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//			throw e;
//		}
//	}

	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}", method = { RequestMethod.POST })
	public ResponseEntity<String> createCollection(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@RequestParam(value = "private", required = false) boolean priv,
			@RequestParam(value = "user", required = false) String user,
			@RequestParam(value = "users", required = false) String sUsers,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			String result = "";
			String collectionId = service.createCollection(collectionName, user, priv, sUsers); 
			if(collectionId!=null){//priv, sUsers, sPasswords)){
				result = "The collection "+collectionName+" [with Id="+collectionId+"] has been successfully created!!";
			}
			else{
				result = "The collection "+collectionName+" has NOT been created. The process has failed!!!!";
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}/overview", method = { RequestMethod.GET })
	public ResponseEntity<String> getCollectionOverview(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@RequestParam(value = "user", required = false) String userName,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			String jsonString = service.getCollectionOverview(collectionName, userName);
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(jsonString, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	@RequestMapping(value = "/e-documentstorage/collection/{collection}", method = { RequestMethod.GET })
	public ResponseEntity<String> getCollection(
			HttpServletRequest request, 
			@PathVariable(value = "collection") String collectionName,
			@RequestParam(value = "user", required = false) String userName,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			String jsonString = service.getCollection(collectionName, userName);
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(jsonString, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}", method = { RequestMethod.DELETE } )
	public ResponseEntity<String> deleteCollection(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@RequestParam(value = "user", required = false) String user,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			String result = "";
			String collection = service.deleteCollection(collectionName, user); 
			if(collection!=null){
				result = collection;
			}
			else{
				result = "There was a problem deleting collection "+collectionName+".";
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}	

	@RequestMapping(value = "/e-documentstorage/{collection}/listDocuments", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> listDocumentsFromCollection(
			HttpServletRequest request, 
			@PathVariable(value = "collection") String collectionName,
			@RequestParam(value = "user", required = false) String userName,
			@RequestParam(value = "limit", required = false, defaultValue="0") int limit,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			String result = service.listDocuments(collectionName, userName, limit);
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}
	
	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}/document/{documentName}", method = { RequestMethod.POST })
	public ResponseEntity<String> addDocumentToCollection(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@PathVariable(value = "documentName") String documentName,
			@RequestParam(value = "user", required = false) String user,
			@RequestParam(value = "input", required = false) String input,
            @RequestBody(required = false) String postBody) throws Exception {
		        	
		try {
			if(input==null){
				input=postBody;
			}

			File inputFile = null;
			double rand = Math.random()*100000;
		    File tempDir = new File(System.getProperty("java.io.tmpdir"));
			if(input!=null){
			    File tempFile = File.createTempFile("ds_plaintext_"+rand, ".txt", tempDir);
			    FileWriter fileWriter = new FileWriter(tempFile, true);
			    BufferedWriter bw = new BufferedWriter(fileWriter);
			    bw.write(input);
			    bw.close();
			    inputFile = tempFile;
			}
			else {
				MultipartFile file1 = null;
				if (request instanceof MultipartHttpServletRequest){
					MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
					file1 = multipartRequest.getFile("inputFile");
					if(file1==null){
						throw LoggedExceptions.generateInteractionLoggedBadRequestException(logger, "No file received in request", "dkt-usage@"+request.getRemoteAddr(), "e-Clustering/generateClusters");
					}
					if (!file1.isEmpty()) {
						try {
						    File tempFile = new File(tempDir + File.separator + file1.getOriginalFilename());
						    tempFile.createNewFile(); 
						    FileOutputStream fos = new FileOutputStream(tempFile); 
						    fos.write(file1.getBytes());
						    fos.close(); 
						    inputFile = tempFile;
						} catch (Exception e) {
							throw LoggedExceptions.generateInteractionLoggedBadRequestException(logger, "Fail at reading input file.", "dkt-usage@"+request.getRemoteAddr(), "e-Clustering/generateClusters");
						}
					} else {
						throw LoggedExceptions.generateInteractionLoggedBadRequestException(logger, "The given file was empty.", "dkt-usage@"+request.getRemoteAddr(), "e-Clustering/generateClusters");
					}
				}
			}
//        	return service.storeFileByFile(storageFileName, tempFile, preffix);

//	        NIFParameterSet nifParameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
//	        Model inModel = ModelFactory.createDefaultModel();
//
//	        if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
//	            rdfConversionService.plaintextToRDF(inModel, nifParameters.getInput(),language, nifParameters.getPrefix());
//	        } else {
//	            inModel = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
//	        }
	        
			String result = "";
			String documentId = service.addDocumentToCollection(collectionName, user, inputFile);
			if(documentId!=null){
				result = "The document [with Id="+documentId+"] has been successfully created!!";
			}
			else{
				result = "The document "+inputFile.getName()+" for collection "+collectionName+" has NOT been created. The process has failed!!!!";
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	
	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}/document/{documentName}", method = { RequestMethod.GET })
	public ResponseEntity<String> getDocumentNIF(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@PathVariable(value = "documentName") String documentName,
			@RequestParam(value = "user", required=false) String user,
			@RequestParam(value = "contentType", required=false) String contentType,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			
			String result = null;
			if(contentType.equalsIgnoreCase("nif")){
				result = service.getNIFDocument(documentName, collectionName, user);
			}
			else{
				result = service.getFileContent(collectionName, user, documentName);
			}

			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}	

	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}/document/{documentName}/update", method = { RequestMethod.POST })
	public ResponseEntity<String> updateDocument(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@PathVariable(value = "documentName") String documentName,
			@RequestParam(value = "user", required = false) String user,
			@RequestParam(value = "input", required = false) String input,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			if(input==null){
				input=postBody;
			}

			File inputFile = null;
			double rand = Math.random()*100000;
		    File tempDir = new File(System.getProperty("java.io.tmpdir"));
			if(input!=null){
			    File tempFile = File.createTempFile("ds_plaintext_"+rand, ".txt", tempDir);
			    FileWriter fileWriter = new FileWriter(tempFile, true);
			    BufferedWriter bw = new BufferedWriter(fileWriter);
			    bw.write(input);
			    bw.close();
			    inputFile = tempFile;
			}
			else {
				MultipartFile file1 = null;
				if (request instanceof MultipartHttpServletRequest){
					MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
					file1 = multipartRequest.getFile("inputFile");
					if(file1==null){
						throw LoggedExceptions.generateInteractionLoggedBadRequestException(logger, "No file received in request", "dkt-usage@"+request.getRemoteAddr(), "e-Clustering/generateClusters");
					}
					if (!file1.isEmpty()) {
						try {
						    File tempFile = new File(tempDir + File.separator + file1.getOriginalFilename());
						    tempFile.createNewFile(); 
						    FileOutputStream fos = new FileOutputStream(tempFile); 
						    fos.write(file1.getBytes());
						    fos.close(); 
						    inputFile = tempFile;
						} catch (Exception e) {
							throw LoggedExceptions.generateInteractionLoggedBadRequestException(logger, "Fail at reading input file.", "dkt-usage@"+request.getRemoteAddr(), "e-Clustering/generateClusters");
						}
					} else {
						throw LoggedExceptions.generateInteractionLoggedBadRequestException(logger, "The given file was empty.", "dkt-usage@"+request.getRemoteAddr(), "e-Clustering/generateClusters");
					}
				}

			}

			String result = "";
			boolean b = service.updateDocument(collectionName, user, documentName, inputFile);
			if(b){
				result = "The document "+documentName+" has been successfully updated!!";
			}
			else{
				result = "The document "+documentName+" has NOT been updated. The process has failed!!!!";
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	@RequestMapping(value = "/e-documentstorage/collection/{collectionName}/document/{documentName}", method = { RequestMethod.DELETE })
	public ResponseEntity<String> deleteDocumentFromCollection(
			HttpServletRequest request, 
			@PathVariable(value = "collectionName") String collectionName,
			@PathVariable(value = "documentName") String documentName,
			@RequestParam(value = "user", required = false) String user,
            @RequestBody(required = false) String postBody) throws Exception {
		try {
			String result = service.deleteDocumentByName(collectionName, documentName, user);
			HttpHeaders responseHeaders = new HttpHeaders();
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}
	
}
