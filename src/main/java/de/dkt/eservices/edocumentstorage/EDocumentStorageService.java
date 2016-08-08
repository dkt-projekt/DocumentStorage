package de.dkt.eservices.edocumentstorage;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.exceptions.LoggedExceptions;
import de.dkt.common.niftools.NIFReader;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.ExternalServiceFailedException;


/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 *
 */
@Component
public class EDocumentStorageService {
	
	Logger logger = Logger.getLogger(EDocumentStorageService.class);

	@Autowired
	DocumentStorage storageService;

	public String createCollection(String collectionName, String user, boolean priv, String sUsers) throws ExternalServiceFailedException {
		return storageService.storeCollection(collectionName, user, priv, sUsers);
	}

//	public String listCollections(String user,int limit){
//		return listCollectionsJSON(user,limit).toString();
//	}
//	
//	public JSONObject listCollectionsJSON(String user,int limit){
//		List<Model> collections = storageService.listCollections(user);
//		List<Model> list2 = new LinkedList<Model>();
//		if(limit>0){
//			int counter = 0;
//			for (Model col: collections) {
//				if(counter<limit){
//					list2.add(col);
//					counter++;
//				}
//			}
//			return NIFManagement.convertCollectionListIntoJSON(list2);
//		}
//		else{
//			return NIFManagement.convertCollectionListIntoJSON(collections);
//		}
//	}

	public String getCollectionOverview(String collectionName, String userName){
		return storageService.getCollectionOverview(collectionName);
	}
		
	public String getCollection(String collectionName, String userName){
		Model m = storageService.getCollection(collectionName); 
		return NIFReader.model2String(m, "ttl");
	}
		
	public String updateCollection(String collectionName, String user){
		return storageService.updateCollection(collectionName,user);
	}

	public String deleteCollection(String collectionName, String user){
		return storageService.deleteCollection(collectionName,user);
	}

	
	
	public String listDocuments(String collectionName,String user,int limit){
		return listDocumentsJSON(collectionName, user, limit).toString();
	}
	
	public JSONObject listDocumentsJSON(String collectionName,String user,int limit){
		List<Model> documents = storageService.listDocuments(collectionName,user);
		List<Model> list2 = new LinkedList<Model>();
		if(limit>0){
			int counter = 0;
			for (Model doc: documents) {
				if(counter<limit){
					list2.add(doc);
					counter++;
				}
			}
			return NIFManagement.convertListIntoJSON(list2);
		}
		else{
			return NIFManagement.convertListIntoJSON(documents);
		}

	}

	public String addDocumentToCollection(String collectionName, String user, File inputFile) {
		try{
			String documentIdentifier = storageService.storeDocument(collectionName, user, inputFile);
			if(documentIdentifier==null){
				throw LoggedExceptions.generateLoggedBadRequestException(logger,"Error storing the document.");
			}
			return documentIdentifier;
		}
		catch(Exception e){
			throw LoggedExceptions.generateLoggedBadRequestException(logger, "User \""+user+"\" has not rights for accessing the collection \""+collectionName+"\"");
		}
	}
	
	public String getNIFDocument(String documentName, String collectionName, String user) {
		Model doc = storageService.getDocument(documentName, collectionName, user);
		return NIFReader.model2String(doc, "TTL");
	}		

    public String getFileContent(String collectionName, String user, String documentName){
        try {
        	return storageService.getFileContent(collectionName, user, documentName);
        } catch (Exception e) {
            throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
    	}
    }

	public String deleteDocumentByName(String collectionName, String documentName, String user) {
       	try{
       		String document = storageService.deleteDocument(collectionName, user, documentName);
       		return document;
       	}
       	catch(Exception e){
       		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
       	}
	}
	
	public boolean updateDocument(String collectionName, String user, String documentName, File inputFile) {
		try{
			return storageService.updateDocument(documentName, collectionName, user, inputFile);
		}
		catch(Exception e){
			throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
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
