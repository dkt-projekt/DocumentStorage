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

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.broker.edocumentstorage.exceptions.ExternalServiceFailedException;
import eu.freme.broker.filemanagement.FileFactory;

/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 */
public class DocumentStorage {

	private static String storageDirectory = "C:\\Users\\jmschnei\\Desktop\\dkt-test\\docStorage\\";
	private static String uriPrefix = "http://dkt.dfki.de/storage/";

	public DocumentStorage(){
	}
	
	public DocumentStorage(String storageDirectory, String uriPrefix){
		this.storageDirectory = storageDirectory;
	}
	
	public static String storeFileByPath(String storageFileName, String inputFileName) throws ExternalServiceFailedException {
		try{
			File inputFil = FileFactory.generateFileInstance(inputFileName);
			
			return storeFileByFile(storageFileName, inputFil);
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

	public static String storeFileByFile(String storageFileName, File inputFile) throws ExternalServiceFailedException {
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

			String nifContent = "";
			
			//TODO Extract the content and generated a NIF representation.
			File fil2 = FileFactory.generateFileInstance(storageDirectory+storageFileName+".nif");
			if(fil2.exists()){
				throw new ExternalServiceFailedException("There is a file with the same name, please rename it!!!");
			}
			if(!fil2.createNewFile()){
				throw new ExternalServiceFailedException("Error at creating the sotrageFile!!!");
			}

			Model outModel = ModelFactory.createDefaultModel();

			//TODO Generate NIF Copiar de donde esté el codigo que hace lo mismo. Creo que en sesame se generaba.
			
			
			
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
		catch(IOException e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
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
