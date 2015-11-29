package eu.freme.broker.edocumentstorage.modules;

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

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.broker.edocumentstorage.exceptions.ExternalServiceFailedException;
import eu.freme.broker.filemanagement.FileFactory;
import eu.freme.broker.niftools.NIFWriter;

/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 */
public class DocumentStorage {

	private static String storageDirectory = "C:\\Users\\jmschnei\\Desktop\\dkt-test\\docStorage\\";
	private static String uriPrefix = "http://dkt.dfki.de/storage/";

	static String IV = "AAAAAAAAAAAAAAAA";
	static String plaintext = "test text 123\0\0\0";
	static String encryptionKey = "0123456789abcdef";
	  
	public DocumentStorage(){
	}
	
	public DocumentStorage(String storageDirectory, String uriPrefix){
		this.storageDirectory = storageDirectory;
	}
	
	public static String storeFileByPath(String storageFileName, String inputFileName, String prefix) throws ExternalServiceFailedException {
		try{
			File inputFil = FileFactory.generateFileInstance(inputFileName);
			
			return storeFileByFile(storageFileName, inputFil, prefix);
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

	public static String storeFileByFile(String storageFileName, File inputFile, String prefix) throws ExternalServiceFailedException {
		try{
			File fil = FileFactory.generateFileInstance(storageDirectory+storageFileName);
			if(fil.exists()){
				throw new ExternalServiceFailedException("There is a file with the same name, please rename it!!!");
			}
			
			if(!fil.createNewFile()){
				throw new ExternalServiceFailedException("Error at creating the sotrageFile!!!");
			}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fil), "utf-8"));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
			String line = br.readLine();
			while(line!=null){
				bw.write(line + "\n");
				line = br.readLine();
			}
			br.close();
			bw.close();

			AutoDetectParser parser = new AutoDetectParser();
//		    BodyContentHandler handler = new BodyContentHandler();
		    ToXMLContentHandler handler = new ToXMLContentHandler();
		    Metadata metadata = new Metadata();
			InputStream stream = new FileInputStream(inputFile);
			String body = "";
		    try{
		        parser.parse(stream, handler, metadata);
		        body = handler.toString();
			} finally {
			    stream.close();            // close the stream
			}

			String nifContent = body;
			
			File fil2 = FileFactory.generateFileInstance(storageDirectory+storageFileName+".nif");
			if(fil2.exists()){
				throw new ExternalServiceFailedException("There is a file with the same name, please rename it!!!");
			}
			if(!fil2.createNewFile()){
				throw new ExternalServiceFailedException("Error at creating the sotrageFile!!!");
			}

			Model outModel = ModelFactory.createDefaultModel();

			String documentURI = "";
			if(prefix==null || prefix.equalsIgnoreCase("")){
				documentURI = "http://dkt.dfki.de/document/";
			}
			else{
				documentURI = prefix;
			}
			documentURI = documentURI + "" + encrypt(storageFileName, encryptionKey);
			NIFWriter.addInitialString(outModel, nifContent, documentURI);
			
			outModel = outModel.write(new FileOutputStream(fil2), "RDF/XML");
			return outModel.toString();
		}
		catch(TikaException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
		catch(SAXException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
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

	public static String getFileContent(String storageFileName) throws ExternalServiceFailedException {
		try{
			File fil = FileFactory.generateFileInstance(storageDirectory+storageFileName);

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

	public static void main(String[] args) throws Exception{
		
//		System.out.println(DocumentStorage.storeTriplet("triplet2", "http://dkt.dfki.de/file2.txt", "http://dkt.dfki.de/ontology#isPartOf", "http://dkt.dfki.de/file3.txt", ""));
//		System.out.println(DocumentStorage.retrieveTriplets("triplet2", null, null, null));
	
	}
}
