package eu.freme.broker.edocumentstorage.api;

import java.io.File;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import eu.freme.broker.edocumentstorage.exceptions.BadRequestException;
import eu.freme.broker.edocumentstorage.exceptions.ExternalServiceFailedException;
import eu.freme.broker.edocumentstorage.modules.DocumentStorage;

/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 *
 */
@Component
public class EDocumentStorageService {
	
    public ResponseEntity<String> storeFileByString(String storageName, String content, String preffix)
            throws ExternalServiceFailedException, BadRequestException {
        try {
        	EDocumentStorageService.checkNotNullOrEmpty(storageName, "storage");
        	EDocumentStorageService.checkNotNullOrEmpty(content, "content");
        	
       		String nifResult = DocumentStorage.storeFileByString(storageName, content, preffix);
       		
           	return EDocumentStorageService.successResponse(nifResult, "RDF/XML");
        } catch (BadRequestException e) {
            throw e;
    	} catch (ExternalServiceFailedException e2) {
    		throw e2;
    	}
    }

    public ResponseEntity<String> storeFileByPath(String storageName, String inputFilePath, String preffix)
            throws ExternalServiceFailedException, BadRequestException {
        try {
        	EDocumentStorageService.checkNotNullOrEmpty(storageName, "No Storage specified");
        	EDocumentStorageService.checkNotNullOrEmpty(inputFilePath, "No inputFilePath specified");
        	
       		String nifResult = DocumentStorage.storeFileByPath(storageName, inputFilePath, preffix);
       		
           	return EDocumentStorageService.successResponse(nifResult, "RDF/XML");
        } catch (BadRequestException e) {
            throw e;
    	} catch (ExternalServiceFailedException e2) {
    		throw e2;
    	}
    }

    public ResponseEntity<String> storeFileByFile(String storageName, File inputFile, String preffix)
            throws ExternalServiceFailedException, BadRequestException {
        try {
        	EDocumentStorageService.checkNotNullOrEmpty(storageName, "No Storage specified");
        	EDocumentStorageService.checkNotNull(inputFile, "No inputFilePath specified");
        	
       		String nifResult = DocumentStorage.storeFileByFile(storageName, inputFile, preffix);
       		
           	return EDocumentStorageService.successResponse(nifResult, "RDF/XML");
        } catch (BadRequestException e) {
            throw e;
    	} catch (ExternalServiceFailedException e2) {
    		throw e2;
    	}
    }

    public ResponseEntity<String> getNifContent(String storageFile)
            throws ExternalServiceFailedException, BadRequestException {
        try {
        	EDocumentStorageService.checkNotNullOrEmpty(storageFile, "stored file");

        	String nifResult = DocumentStorage.getNIFContent(storageFile);
       		
           	return EDocumentStorageService.successResponse(nifResult, "RDF/XML");
        } catch (BadRequestException e) {
            throw e;
    	} catch (ExternalServiceFailedException e2) {
    		throw e2;
    	}
    }

    public ResponseEntity<String> getFileContent(String storageFile)
            throws ExternalServiceFailedException, BadRequestException {
        try {
        	EDocumentStorageService.checkNotNullOrEmpty(storageFile, "stored file");

        	String nifResult = DocumentStorage.getNIFContent(storageFile);
       		
           	return EDocumentStorageService.successResponse(nifResult, "RDF/XML");
        } catch (BadRequestException e) {
            throw e;
    	} catch (ExternalServiceFailedException e2) {
    		throw e2;
    	}
    }
    
    public static void checkNotNullOrEmpty (String param, String message) throws BadRequestException {
    	if( param==null || param.equals("") ){
            throw new BadRequestException("No "+message+" param specified");
    	}
    }

    public static void checkNotNull (Object param, String message) throws BadRequestException {
    	if( param==null ){
            throw new BadRequestException("No "+message+" param specified");
    	}
    }

    public static ResponseEntity<String> successResponse(String body, String contentType) throws BadRequestException {
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.add("Content-Type", contentType);
    	ResponseEntity<String> response = new ResponseEntity<String>(body, responseHeaders, HttpStatus.OK);
    	return response;
    }
}
