package eu.freme.broker.edocumentstorage.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.broker.edocumentstorage.exceptions.BadRequestException;
import eu.freme.broker.edocumentstorage.exceptions.ExternalServiceFailedException;
import eu.freme.broker.eservices.BaseRestController;
import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.broker.tools.NIFParameterFactory;
import eu.freme.broker.tools.RDFELinkSerializationFormats;
import eu.freme.broker.tools.RDFSerializationFormats;
import eu.freme.common.conversion.rdf.RDFConversionService;

@RestController
public class EDocumentStorageServiceStandAlone extends BaseRestController {
	
	@Autowired
	EDocumentStorageService service;
	
	@Autowired
	RDFConversionService rdfConversionService;

	@Autowired
	NIFParameterFactory nifParameterFactory;

	@Autowired
	RDFSerializationFormats rdfSerializationFormats;

	@Autowired
	RDFELinkSerializationFormats rdfELinkSerializationFormats;
	
	@Autowired
	ExceptionHandlerService exceptionHandlerService;

	@RequestMapping(value = "/e-documentstorage/storeFile", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> storeData(
			@RequestParam(value = "storageFileName", required = false) String storageFileName,
			@RequestParam(value = "inputFilePath", required = false) String inputFilePath,
			@RequestParam(value = "preffix", required = false) String preffix,
            @RequestBody(required = false) String postBody) throws Exception {

		EDocumentStorageService.checkNotNullOrEmpty(inputFilePath, "input data type");
		EDocumentStorageService.checkNotNullOrEmpty(storageFileName, "storage file name");

		//TODO Something with the NIF things.
		
        try {
        	return service.storeFileByPath(storageFileName, inputFilePath, preffix);
        } catch (BadRequestException e) {
            throw e;
        } catch (ExternalServiceFailedException e) {
            throw e;
        }
	}

	@RequestMapping(value = "/e-documentstorage/storeFile", method = { RequestMethod.PUT })
	public ResponseEntity<String> storeData(
			@RequestParam(value = "storageFileName", required = false) String storageFileName,
			@RequestParam(value = "preffix", required = false) String preffix,
            @RequestBody(required = false) String postBody) throws Exception {

		EDocumentStorageService.checkNotNullOrEmpty(postBody, "body content");
		EDocumentStorageService.checkNotNullOrEmpty(storageFileName, "storage file name");

		//TODO Something with the NIF things.
		
		double rand = Math.random()*100000;
	    File tempDir = new File(System.getProperty("java.io.tmpdir"));
	    File tempFile = File.createTempFile(rand+"", ".tmp", tempDir);
	    FileWriter fileWriter = new FileWriter(tempFile, true);
//	    System.out.println(tempFile.getAbsolutePath());
	    BufferedWriter bw = new BufferedWriter(fileWriter);
	    bw.write(postBody.toString());
	    bw.close();
	    
        try {
        	return service.storeFileByFile(storageFileName, tempFile, preffix);
        } catch (BadRequestException e) {
            throw e;
        } catch (ExternalServiceFailedException e) {
            throw e;
        }
	}

	@RequestMapping(value = "/e-documentstorage/getFileContent", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> getFileContent(
			@RequestParam(value = "storageFileName", required = false) String storageFileName,
            @RequestBody(required = false) String postBody) throws Exception {

		EDocumentStorageService.checkNotNullOrEmpty(storageFileName, "storage file name");

		//TODO Something with the NIF things.
		
        try {
        	return service.getFileContent(storageFileName);
        } catch (BadRequestException e) {
            throw e;
        } catch (ExternalServiceFailedException e) {
            throw e;
        }
	}

	@RequestMapping(value = "/e-documentstorage/getNIFFileContent", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> getNIFFileContent(
			@RequestParam(value = "storageFileName", required = false) String storageFileName,
            @RequestBody(required = false) String postBody) throws Exception {

		EDocumentStorageService.checkNotNullOrEmpty(storageFileName, "storage file name");

		//TODO Something with the NIF things.
		
        try {
        	return service.getNifContent(storageFileName);
        } catch (BadRequestException e) {
            throw e;
        } catch (ExternalServiceFailedException e) {
            throw e;
        }
	}
}
