package de.dkt.eservices.edocumentstorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import de.dkt.common.exceptions.LoggedExceptions;
import de.dkt.common.filemanagement.FileFactory;
import de.dkt.common.niftools.DKTNIF;
import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;
import de.dkt.eservices.edocumentstorage.ddbb.Collection;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
////import eu.freme.broker.niftools.NIFWriter;
import eu.freme.common.exception.ExternalServiceFailedException;

/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 */
@Component
public class DocumentStorage {

	Logger logger = Logger.getLogger(DocumentStorage.class);

//	private static String storageDirectory = "C:\\Users\\jmschnei\\Desktop\\dkt-test\\docStorage\\";
//	private static String storageDirectory = "/Users/jumo04/Documents/DFKI/DKT/dkt-test/docstorage/";
	protected static String storageDirectory = "/Users/jumo04/Documents/DFKI/DKT/dkt-test/testComplete/storage/";

	private static String websiteDirectory = "/var/www/html/data/dkt-documents/collectionName/";

	private static String collectionsInformationFile = "collectionInformationFile.txt";
	
	private static String uriPrefix = "http://dkt.dfki.de/storage/collection/";

	static String IV = "AAAAAAAAAAAAAAAA";
	static String plaintext = "test text 123\0\0\0";
	static String encryptionKey = "0123456789abcdef";

	
	private HashMap<String,Collection> collections;
	
	public DocumentStorage(){
		String OS = System.getProperty("os.name");
		if(OS.startsWith("Mac")){
			storageDirectory = "/Users/jumo04/Documents/DFKI/DKT/dkt-test/testComplete/storage22/";
		}
		else if(OS.startsWith("Windows")){
			storageDirectory = "C:/tests/storage/";
		}
		else if(OS.startsWith("Linux")){
			storageDirectory = "/opt/storage/";
		}
		
		initializeCollectionsInformation();
	}
	
	public DocumentStorage(String storageDirectory, String uriPrefix){
		this.storageDirectory = storageDirectory;
		initializeCollectionsInformation();
	}

	public boolean initializeCollectionsInformation(){
		try{
			collections = new HashMap<String, Collection>();
			BufferedReader br = FileFactory.generateBufferedReaderInstance(storageDirectory + collectionsInformationFile, "utf-8");
			String content = "";
			String line = br.readLine();
			while(line!=null){
				content += line + "\n";
				line = br.readLine();
			}
			br.close();
			JSONObject collectionsJSON = new JSONObject(content);
			JSONObject colsJSON = collectionsJSON.getJSONObject("collections");
			Set<String> jsonKeys = colsJSON.keySet();
			for (String jk : jsonKeys) {
				JSONObject collec = colsJSON.getJSONObject(jk);
				Collection c = new Collection(collec);
				collections.put(c.getCollectionName(), c);
			}
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateCollectionsInformation(){
		try{
			JSONObject colsJSON = new JSONObject();
			Set<String> keys = collections.keySet();
			for (String k : keys) {
				colsJSON.put(k, collections.get(k).getJSONObject());
			}
			JSONObject collectionsJSON = new JSONObject();
			collectionsJSON.put("collections", colsJSON);
			
	
			BufferedWriter bw = FileFactory.generateBufferedWriterInstance(storageDirectory+collectionsInformationFile, "utf-8", false);
			bw.write(collectionsJSON.toString());
			bw.close();
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public String storeCollection(String collectionName, String user, boolean priv, String sUsers) throws ExternalServiceFailedException {
        try {
        	String prefix = "";
        	
        	String collectionFile = collectionName+".cfe";
        	File fil = null;
        	try{
        		fil = FileFactory.generateFileInstance(storageDirectory+collectionFile);//cfe means collection file extension
        		throw LoggedExceptions.generateLoggedBadRequestException(logger,"There is a file with the same name, please rename it!!!");
        	}
        	catch(Exception e){
        		fil = FileFactory.generateOrCreateFileInstance(storageDirectory+collectionFile);//cfe means collection file extension
        	}
//   			File fsrDir = FileFactory.generateFileInstance(storageDirectory);
//   			File newFil = new File(fsrDir,collectionFile);
//   			if(!newFil.exists()){
//   				if(!newFil.createNewFile()){
//   	        		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"Error at creating the storageFile!!!");
//   				}
//   			}

   			String collectionURI = "";
//   			if(prefix==null || prefix.equalsIgnoreCase("")){
//   				//        				documentURI = "http://dkt.dfki.de/document/";
//   				int cnt = collections.size();
//   				collectionURI = uriPrefix+cnt;
//   			}
//   			else{
//   				collectionURI = prefix;
//   			}
			collectionURI = uriPrefix+collectionName;
   			Model outModel = NIFManagement.createDefaultCollectionModel(collectionURI);
   			////        			NIFWriter.addInitialString(outModel, nifContent, documentURI);
 //  			StringWriter sw = new StringWriter();
   			outModel = outModel.write(new FileOutputStream(fil), "Turtle");
//   			outModel = outModel.write(sw, "RDF/XML");
//   			return sw.toString();

   			List<String> users = new LinkedList<String>();
   			users.add(user);
   			if(sUsers!=null && !sUsers.equalsIgnoreCase("")){
   	   			String parts[] = sUsers.split(",");
   	   			for (String k : parts) {
   					users.add(k);
   				}
   			}
   			List<String> documents = new LinkedList<String>();
   			Collection c = new Collection(collectionName, priv, users, documents, fil.getAbsolutePath());
   			collections.put(collectionName, c);
   			updateCollectionsInformation();
   			return collectionName;
        } catch (Exception e) {
        	e.printStackTrace();
        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
        }
	}

	public List<Model> listCollections(String user) throws ExternalServiceFailedException {
        try {
        	List<Model> list = new LinkedList<Model>();
   			File fsrDir = FileFactory.generateFileInstance(storageDirectory);
   			if(!fsrDir.exists()){
   				throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"There is no Storage Directory to find collections!");
   			}

   			File[] collectionFiles = fsrDir.listFiles();
   			for (File f : collectionFiles) {
   				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
   				String line = br.readLine();
   				String content = "";
   				while(line!=null){
   					content += line + "\n";
   					line = br.readLine();
   				}
   				br.close();
   				
   				Model aux = NIFReader.extractModelFromFormatString(content, RDFSerialization.TURTLE);
   				list.add(aux);
			}
        	return list;
    	} catch (Exception e) {
    		e.printStackTrace();
        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
    	}
	}

	public Model getCollection(String collectionName) throws ExternalServiceFailedException {
        try {
        	String collectionFile = collectionName+".cfe";
   			File colFil = FileFactory.generateFileInstance(storageDirectory + collectionFile);
   			if(!colFil.exists()){
   				throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"There is no collection with this name.");
   			}

   			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(colFil), "utf-8"));
   			String line = br.readLine();
   			String content = "";
   			while(line!=null){
   				content += line + "\n";
   				line = br.readLine();
   			}
   			br.close();

   			Model collectionModel = NIFReader.extractModelFromFormatString(content, RDFSerialization.TURTLE);
        	return collectionModel;
    	} catch (Exception e) {
    		e.printStackTrace();
        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
    	}
	}

	public String getCollectionOverview(String collectionName) throws ExternalServiceFailedException {
        try {
    		Collection c = collections.get(collectionName);
    		if(c!=null){
    			return c.getJSONObject().toString();
    		}
        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, "There is no collection with this name");
    	} catch (Exception e) {
    		e.printStackTrace();
        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
    	}
	}

	public String deleteCollection(String collectionName, String user) throws ExternalServiceFailedException {
		if(checkCollectionPermision(collectionName, user)){
			try{
		    	String collectionFile = collectionName+".cfe";
				File colFil = FileFactory.generateFileInstance(storageDirectory + collectionFile);
				if(!colFil.exists()){
					throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"There is no collection with this name.");
				}
				Model m = getCollection(collectionName);
				if(!colFil.delete()){
					throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"There was a problem deleting the collection file.");
				}
				
				collections.remove(collectionName);
				updateCollectionsInformation();
				
				return NIFReader.model2String(m, "Turtle");
	    	} catch (Exception e) {
	    		e.printStackTrace();
	        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
	    	}
		}
		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, "The user has not permission to delete this collection.");
	}

	public String updateCollection(String collectionName, String user, Model collectionModel) {
		if(checkCollectionPermision(collectionName, user)){
        	String prefix = "";
        	
        	String collectionFile = collectionName+".cfe";
        	File fil = null;
        	try{
        		fil = FileFactory.generateFileInstance(storageDirectory+collectionFile);//cfe means collection file extension
        	}
        	catch(Exception e){
        		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"There is a file with the same name, please rename it!!!");
        	}
   			try {
				collectionModel = collectionModel.write(new FileOutputStream(fil), "Turtle");
			} catch (FileNotFoundException e) {
        		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger,"Error writting the model to the collection file");
			}
   			return collectionName;
		}
		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, "The user has not permission to delete this collection.");
	}

	public boolean checkCollectionPermision(String collectionName, String user) {
		Collection c = collections.get(collectionName);
		if(c!=null){
			if(!c.isPriv() || c.getUsers().contains(user)){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public String storeDocument(String collectionName, String user, String documentName, File inputFile) throws ExternalServiceFailedException {
		if(checkCollectionPermision(collectionName, user)){
			try{
				String storageFileName = inputFile.getName();
				String documentPath = storageDirectory+storageFileName;
	        	File fil = null;
	        	try{
	        		fil = FileFactory.generateFileInstance(documentPath);//cfe means collection file extension
	        		throw LoggedExceptions.generateLoggedBadRequestException(logger,"There is a file with the same name, please rename the document!!!");
	        	}
	        	catch(Exception e){
	        		fil = FileFactory.generateOrCreateFileInstance(documentPath);//cfe means collection file extension
	        	}
				FileUtils.copyFile(inputFile, fil);

				AutoDetectParser parser = new AutoDetectParser();
				BodyContentHandler handler = new BodyContentHandler();
				//ToXMLContentHandler handler = new ToXMLContentHandler();
				Metadata metadata = new Metadata();
				InputStream stream = new FileInputStream(inputFile);
				String body = "";
				try{
					parser.parse(stream, handler, metadata);
					body = handler.toString();
				} finally {
					stream.close();            // close the stream
				}

				Model collectionModel = getCollection(collectionName);
				if(collections.get(collectionName).getDocuments().contains(documentName)){
					documentName += "_2";
				}
	   			String documentPrefix = NIFManagement.extractCollectionURI(collectionModel)+"/document/"+documentName;
				Model documentModel = NIFManagement.createDocumentModel(collectionModel, documentName, documentPrefix, body, documentPath);
				String documentURI = NIFManagement.extractCompleteDocumentURI(documentModel);
				NIFManagement.addDocumentToCollection(collectionModel, documentModel);

//				System.out.println("COLLECTION: " + NIFReader.model2String(collectionModel, "Turtle"));
				updateCollection(collectionName, user, collectionModel);

	   			collections.get(collectionName).addDocument(documentName);
	   			updateCollectionsInformation();

	   			/**	
	   			 * 
	   			 * 	This part is only needed if we want to store also the document into a separate NIF file.
	   			 */
//	   			File fil2 = null;
//	        	try{
//	        		fil2 = FileFactory.generateFileInstance(storageDirectory+storageFileName+".nif");//cfe means collection file extension
//	        		throw LoggedExceptions.generateLoggedBadRequestException(logger,"There is a file with the same name, please rename the document!!!");
//	        	}
//	        	catch(Exception e){
//	        		fil2 = FileFactory.generateOrCreateFileInstance(storageDirectory+storageFileName+".nif");//cfe means collection file extension
//	        	}
//	   			StringWriter sw = new StringWriter();
//	   			documentModel = documentModel.write(sw, "Turtle");
//	   			String nifContent = sw.toString();
//	   			
//	   			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fil2), "utf-8"));
//	   			bw.write(nifContent+"\n");
//	   			bw.close();
	   			
	   			/**
	   			 * This is only needed if we want to return the NIF content of the created document.
	   			 */
	   			//return nifContent;
	   			return documentURI;
			}
//			catch(TikaException|SAXException|FileNotFoundException e){
//				e.printStackTrace();
//				throw new ExternalServiceFailedException(e.getMessage());
//			}
			catch(Exception e){
				e.printStackTrace();
				throw new ExternalServiceFailedException(e.getMessage());
			}
		}
		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, "The user has not permission to delete this document.");
	}

	public boolean updateDocument(String documentName, String collectionName, String user, File inputFile) throws ExternalServiceFailedException {
//		if(checkCollectionPermision(collectionName, user)){
//			try {
//				String storageFileName = inputFile.getName();
//				File fil = FileFactory.generateFileInstance(storageDirectory+storageFileName);
//				if(fil!=null && fil.exists()){
//					throw new ExternalServiceFailedException("There is a file with the same name, please rename it!");
//				}
//				if(!fil.exists()){
//					if(!fil.createNewFile()){
//						throw new ExternalServiceFailedException("Error at creating the storageFile!!!");
//					}
//				}
//
//				FileUtils.copyFile(inputFile, fil);
//
//				AutoDetectParser parser = new AutoDetectParser();
//				//			    BodyContentHandler handler = new BodyContentHandler();
//				ToXMLContentHandler handler = new ToXMLContentHandler();
//				Metadata metadata = new Metadata();
//				InputStream stream = new FileInputStream(inputFile);
//				String body = "";
//				try{
//					parser.parse(stream, handler, metadata);
//					body = handler.toString();
//				} finally {
//					stream.close();            // close the stream
//				}
//
//				Model collectionModel = getCollection(collectionName);
//	   			String documentURI = NIFManagement.extractCollectionURI(collectionModel)+"/document"+collections.get(collectionName).getDocuments().size();
//				Model documentModel = NIFManagement.createDocumentModel(collectionModel, documentName, documentURI);
////				NIFManagement.addDocumentToCollection(collectionModel, documentModel);
//
//	   			collections.get(collectionName).addDocument(documentName);
//	   			updateCollectionsInformation();
//
//	   			
//	   			return NIFManagement.updateDocument(collectionModel, documentName, inputFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//	        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
//			}
//		}
//		throw new ExternalServiceFailedException("The user has not permission to read this document.");
		return true;
    }
	
	public Model getDocument(String documentName, String collectionName, String user) throws ExternalServiceFailedException {
		if(checkCollectionPermision(collectionName, user)){
			try {
	   			Model collectionModel = getCollection(collectionName);//NIFReader.extractModelFromFormatString(content, RDFSerialization.TURTLE);
	   			Model documentModel = NIFManagement.extractDocumentModel(collectionModel,documentName);
				return documentModel;
			} catch (Exception e) {
				e.printStackTrace();
	        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
			}
		}
		throw new ExternalServiceFailedException("The user has not permission to read this document.");
	}
	
	public List<Model> listDocuments(String collectionName,String userName) throws ExternalServiceFailedException {
        try {
        	Model collection = getCollection(collectionName);
        	List<Model> documents = NIFManagement.extractDocumentsModels(collection); 
        	return documents;
    	} catch (Exception e) {
    		e.printStackTrace();
        	logger.error(e.getMessage());
    		throw new ExternalServiceFailedException(e.getMessage());
    	}
	}
	
	public String getFileContent(String collectionName, String user, String documentName) throws ExternalServiceFailedException {
		if(checkCollectionPermision(collectionName, user)){
			try {
	   			Model documentModel = getDocument(documentName, collectionName, user);
	   			String filePath = NIFManagement.extractSourceFilePath(documentModel);
				return filePath;
			} catch (Exception e) {
				e.printStackTrace();
	        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
			}
		}
		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, "The user has not permission to read this document.");
	}

	public String deleteDocument(String collectionName, String user, String documentName) {
		if(checkCollectionPermision(collectionName, user)){
			try {
				Model collectionModel = getCollection(collectionName);
				Model documentModel = NIFManagement.deleteDocument(documentName, collectionModel);
				
				String documentPath = NIFManagement.extractSourceFilePath(documentModel);
				File f = FileFactory.generateFileInstance(documentPath);
				if(!f.delete()){
					logger.error("ERROR at deleting document file from the system storage.");
				}
				updateCollection(collectionName, user, collectionModel);
				return NIFReader.model2String(documentModel, "TTL");
			} catch (Exception e) {
				e.printStackTrace();
	        	throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, e.getMessage());
			}
		}
		throw LoggedExceptions.generateLoggedExternalServiceFailedException(logger, "The user has not permission to read this document.");
	}


	public static byte[] encrypt(String plainText, String encryptionKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
		return cipher.doFinal(plainText.getBytes("UTF-8"));
	}

	public static String decrypt(byte[] cipherText, String encryptionKey) throws Exception{
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
		return new String(cipher.doFinal(cipherText),"UTF-8");
	}

	public static String getNIFContent(String storageFileName) throws ExternalServiceFailedException {
		try{
//			File fil = FileFactory.generateFileInstance(storageDirectory+storageFileName+".nif");
//
//			Model inModel = ModelFactory.createDefaultModel();
//			inModel = inModel.read(new FileInputStream(fil), "RDF/XML");
//
//			return inModel;
			File fil = FileFactory.generateFileInstance(storageDirectory+storageFileName+".nif");

			String total = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fil), "utf-8"));
			String line = br.readLine();
			while(line!=null){
				total = total + "\n" + line;
				line = br.readLine();
			}
			br.close();
			return total;
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
		catch(IOException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}

	public static File getFile(String storageFileName) throws ExternalServiceFailedException {
		try{
			File fil = FileFactory.generateFileInstance(storageDirectory+storageFileName);
			if(fil==null){
				throw new ExternalServiceFailedException("File nor retrieved.");
			}
			return fil;
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
		catch(IOException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}

}
