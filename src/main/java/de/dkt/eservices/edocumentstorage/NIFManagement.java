package de.dkt.eservices.edocumentstorage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import de.dkt.common.exceptions.LoggedExceptions;
import de.dkt.common.niftools.DKTNIF;
import de.dkt.common.niftools.NIF;
import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.exception.BadRequestException;

public class NIFManagement {

	public static void setPrefixes(Model model){
		model.setNsPrefix("nif", RDFConstants.nifPrefix);
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	}
	
	public static Model createDefaultCollectionModel(String prefix){
		Model model = ModelFactory.createDefaultModel();
		setPrefixes(model);
		String uri = prefix;
		Resource resource = model.createResource(uri);
		Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
//		resource.addProperty(type,model.createResource(NIF.ContextCollection));
		Statement stmt = model.createStatement(resource, type, NIF.ContextCollection);
		model.add(stmt);
		return model;
	}

	/**
	 * 
	 * 	public static Model initializeOutputModel(){
		Model model = ModelFactory.createDefaultModel();
		//TODO Add NIF namespaces and more.
        Map<String,String> prefixes = new HashMap<String, String>();
        prefixes.put("xsd", "<http://www.w3.org/2001/XMLSchema#>");
        prefixes.put("nif", "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>");
        prefixes.put("dktnif", "http://dkt.dfki.de/ontologies/nif#");
        //prefixes.put("dfkinif", "<http://persistence.dfki.de/ontologies/nif#>");
        //prefixes.put("dbpedia-fr", "<http://fr.dbpedia.org/resource/>");
        //prefixes.put("dbc", "<http://dbpedia.org/resource/Category:>");
        //prefixes.put("dbpedia-es", "<http://es.dbpedia.org/resource/>");
		//prefixes.put("itsrdf", "<http://www.w3.org/2005/11/its/rdf#>");
		//prefixes.put("dbpedia", "<http://dbpedia.org/resource/>");
		//prefixes.put("rdfs", "<http://www.w3.org/2000/01/rdf-schema#>");
		//prefixes.put("dbpedia-de", "<http://de.dbpedia.org/resource/>");
		//prefixes.put("dbpedia-ru", "<http://ru.dbpedia.org/resource/>");
		////prefixes.put("freme-onto", "<http://freme-project.eu/ns#>");
		//prefixes.put("dbpedia-nl", "<http://nl.dbpedia.org/resource/>");
		//prefixes.put("dcterms", "<http://purl.org/dc/terms/>");
		//prefixes.put("dbpedia-it", "<http://it.dbpedia.org/resource/>");
        
        model.setNsPrefixes(prefixes);
        
		return model;
	}
	 */
	public static Model createDocumentModel(Model collectionModel, String documentName, String prefix, String content, String documentPath) {
		Model model = NIFWriter.initializeOutputModel();
		NIFWriter.addInitialString(model, content, prefix);
		setPrefixes(model);
		String documentURI = extractCompleteDocumentURI(model);
		Resource documentURIResource = collectionModel.createResource(documentURI);
		model.add(model.createLiteralStatement(documentURIResource, DKTNIF.DocumentPath, documentPath));
		model.add(model.createLiteralStatement(documentURIResource, DKTNIF.DocumentName, documentName));
		return model;
	}

	public static void addDocumentToCollection(Model collectionModel, Model documentModel) {
//		System.out.println("COLLECTION: " + NIFReader.model2String(collectionModel, "Turtle"));
//		System.out.println("DOCUMENT " + NIFReader.model2String(documentModel, "Turtle"));

		String collectionURI = extractCollectionURI(collectionModel);
		String documentURI = extractCompleteDocumentURI(documentModel);
		Resource collectionURIResource = collectionModel.createResource(collectionURI);
		collectionModel.add(collectionURIResource, NIF.hasContext, documentURI);
		collectionModel.add(documentModel);
//		System.out.println("COLLECTION: " + NIFReader.model2String(collectionModel, "Turtle"));
	}

	public static String extractCompleteDocumentURI(Model documentModel) {
		StmtIterator iter = documentModel.listStatements(null, RDF.type, documentModel.getResource(NIF.Context.getURI()));
		while(iter.hasNext()){
			Resource contextRes = iter.nextStatement().getSubject();
			String uri = contextRes.getURI();
			return uri;
		}
		return null;
	}

	public static Model extractDocumentModel(Model collectionModel, String documentName) {
		Model documentModel = ModelFactory.createDefaultModel();
		documentModel.setNsPrefixes(collectionModel.getNsPrefixMap());
		ResIterator subjects = collectionModel.listSubjectsWithProperty(DKTNIF.DocumentName, documentName);
		while(subjects.hasNext()){
			Resource sub = subjects.next();
			StmtIterator iter = sub.listProperties();
			documentModel.add(iter);
		}
		return documentModel;
	}

	public static List<Model> extractDocumentsModels(Model collectionModel) {
		List<Model> documents = new LinkedList<Model>();
		
		ResIterator subjects = collectionModel.listSubjectsWithProperty(DKTNIF.DocumentName);
		while(subjects.hasNext()){
			Model documentModel = ModelFactory.createDefaultModel();
			documentModel.setNsPrefixes(collectionModel.getNsPrefixMap());
			Resource sub = subjects.next();
			StmtIterator iter = sub.listProperties();
			documentModel.add(iter);
			documents.add(documentModel);
		}
		return documents;
	}

	public static String extractCollectionURI(Model collectionModel) {
		String str = null;
        StmtIterator iter = collectionModel.listStatements(null, RDF.type, NIF.ContextCollection);
        boolean textFound = false;
        while(iter.hasNext() && !textFound){
        	Statement stmt = iter.nextStatement();
//        	System.out.println("Subject: " + stmt.getSubject());
//        	System.out.println("Predicate: " + stmt.getPredicate());
//        	System.out.println("Object: " + stmt.getObject());
            Resource contextRes = stmt.getSubject();
            if (contextRes != null) {
            	str = contextRes.getURI();
            	textFound = true;
            }
        }
		return str;
	}

	public static String extractDocumentURI(Model documentModel) {
		String str = null;
        StmtIterator iter = documentModel.listStatements(null, RDF.type, NIF.Context);
        boolean textFound = false;
        while (!textFound) {
            Resource contextRes = iter.nextStatement().getSubject();
            if (contextRes != null) {
                str = contextRes.getURI();
                textFound = true;
            }
        }
		return str;
	}

	public static String extractSourceFilePath(Model documentModel) {
		String documentURI = extractDocumentURI(documentModel);
        Statement stmt = documentModel.getProperty(documentModel.createResource(documentURI), DKTNIF.DocumentPath);
        String path = stmt.getObject().asLiteral().getString();
		return path;
	}

	public static Model deleteDocument(String documentName, Model collectionModel) {
		Model documentModel = NIFManagement.extractDocumentModel(collectionModel,documentName);
		collectionModel.remove(documentModel);
		String collectionURI = extractCollectionURI(collectionModel);
		String documentURI = extractCompleteDocumentURI(documentModel);
		Resource collectionURIResource = collectionModel.createResource(collectionURI);
		collectionModel.remove(collectionModel.listStatements(collectionURIResource, NIF.hasContext, documentURI));
//		ResIterator subjects = collectionModel.listSubjectsWithProperty(DKTNIF.DocumentName, documentName);
//		while(subjects.hasNext()){
//			Resource sub = subjects.next();
//			StmtIterator iter = sub.listProperties();
//			documentModel.add(iter);
//			collectionModel.remove(iter);
//		}
		return documentModel;
	}

	public static boolean updateDocument(Model collectionModel, String documentName, String content) {
		// TODO Auto-generated method stub
		return false;
	}

	public static JSONObject convertListIntoJSON(List<Model> list) {
		JSONObject obj = new JSONObject();
		JSONObject joCollections = new JSONObject();
		int i=0;
//		System.out.println(list.size());
		
		for (Model m: list) {
//			if(o instanceof User){
////				System.out.println("-------is user:");
//				User u = (User) o;
//				joUsers.put("user"+(i+1), u.getJSONObject());
//			}
//			else if(o instanceof Collection){
////				System.out.println("-------is collection:");
//				Collection c = (Collection) o;
//				joCollections.put("collection"+(i+1), c.getJSONObject());
//			}
//			else if(o instanceof Document){
////				System.out.println("-------is document:");
//				Document d = (Document) o;
//				joDocuments.put("document"+(i+1), d.getJSONObject());
//			}
//			else if(o instanceof NLPModel){
//				NLPModel d = (NLPModel) o;
//				joModels.put("model"+(i+1), d.getJSONObject());
//			}
//			else{
//				System.out.println("ERROR: element type not supported.");
//			}
//			i++;
		}
		if(joCollections.length()>0){
			obj.put("collections", joCollections);
		}
		return obj;		
	}
	
}
