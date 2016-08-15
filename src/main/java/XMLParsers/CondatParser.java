package XMLParsers;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;


public class CondatParser {
	
	/*
	static HashMap<String, HashMap<String, String>> xmlMap = new HashMap<String, HashMap<String, String>>();
	
	public static void parseXML(String filePath){
		
		HashMap<String, String> hm = new HashMap<String, String>();
		
		try {	
			File inputFile = new File(filePath);
		    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(inputFile);
		    doc.getDocumentElement().normalize();
		    NodeList nList = doc.getElementsByTagName("OM_FIELD");
         
		    for (int temp = 0; temp < nList.getLength(); temp++) {
		    	Node nNode = nList.item(temp);
		    	if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    		Element eElement = (Element) nNode;
		    		if (eElement.getAttribute("Name") != null){
		    			if (eElement.getAttribute("Name").equals("Title")){
		    				hm.put("Title", eElement.getTextContent());
		    			}
		    			else if (eElement.getAttribute("Name").equals("Text")){
		    				hm.put("Text", eElement.getTextContent());
		    			}
		    			else if (eElement.getAttribute("Name").equals("CreationDate")){
		    				hm.put("CreationDate", eElement.getTextContent());
		    			}
		    		}
               
		    	}
		    }
		    xmlMap.put(filePath, hm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	public static Model parseXML2NIFModel(String filePath){
		
		/*
		 * This doesn't do anything yet with the metadata that is present in the condat XMLs (title, creationDate, etc.)
		 */
		Model nifModel = ModelFactory.createDefaultModel();
		try {
			String extractedText = null;
			File inputFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(inputFile);
		    doc.getDocumentElement().normalize();
		    NodeList nList = doc.getElementsByTagName("OM_FIELD");
		    
		    for (int temp = 0; temp < nList.getLength(); temp++) {
		    	Node nNode = nList.item(temp);
		    	if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    		Element eElement = (Element) nNode;
		    		if (eElement.getAttribute("Name") != null){
		    			if (eElement.getAttribute("Name").equals("Text")){
		    				extractedText = eElement.getTextContent();
		    			}
		    		}
		    	}
		    }
		    NIFWriter.addInitialString(nifModel, extractedText, "http://dkt.dfki.de/examples/");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nifModel;
		
	}
	
		
	public static void main(String[] args){

		String filePath = "C:\\Users\\pebo01\\Desktop\\Condat_Data\\read_WR_1_3037889.xml";
		Model nifModel = parseXML2NIFModel(filePath);
		
		System.out.println("DEBUG nifModel:\n" + NIFReader.model2String(nifModel, RDFSerialization.TURTLE));
		/*
		parseXML(filePath);
		HashMap<String, String> hm = xmlMap.get(filePath);
		System.out.println("File:" + filePath);
		System.out.println("Title:" + hm.get("Title"));
		*/
		
		
	}		


}
