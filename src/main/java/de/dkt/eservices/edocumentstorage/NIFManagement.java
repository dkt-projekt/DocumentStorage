package de.dkt.eservices.edocumentstorage;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import de.dkt.common.niftools.DKTNIF;
import de.dkt.common.niftools.NIF;
import de.dkt.common.niftools.NIFReader;
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
		resource.addProperty(type,model.createResource(NIF.ContextCollection));
		return model;
	}

	public static Model createDocumentModel(Model collectionModel, String documentName, String prefix) {
		Model model = ModelFactory.createDefaultModel();
		setPrefixes(model);
		
		String uri = prefix;
		Resource resource = model.createResource(uri);

		Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		resource.addProperty(type,model.createResource(NIF.ContextCollection));

//		if (language == null) {
//			resource.addProperty(
//					model.createProperty(RDFConstants.nifPrefix + "isString"),
//					model.createLiteral(plaintext));
//		} else {
//			resource.addProperty(
//					model.createProperty(RDFConstants.nifPrefix + "isString"),
//					model.createLiteral(plaintext, language));
//		}
//
//		Literal beginIndex = model.createTypedLiteral(new Integer(0),
//				XSDDatatype.XSDnonNegativeInteger);
//		resource.addProperty(
//				model.createProperty(RDFConstants.nifPrefix + "beginIndex"),
//				beginIndex);
//		Literal endIndex = model.createTypedLiteral(
//				new Integer(plaintext.length()),
//				XSDDatatype.XSDnonNegativeInteger);
//		resource.addProperty(
//				model.createProperty(RDFConstants.nifPrefix + "endIndex"),
//				endIndex);
		
		return model;
	}

	public static void addDocumentToCollection(Model collectionModel, Model documentModel) {
		String collectionURI = extractCollectionURI(collectionModel);
		String documentURI = NIFReader.extractDocumentURI(documentModel);
		Resource collectionURIResource = collectionModel.createResource(collectionURI);
		collectionModel.add(collectionURIResource, NIF.hasContext, documentURI);
		collectionModel.add(documentModel);
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
        while (!textFound) {
            Resource contextRes = iter.nextStatement().getSubject();
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
		Model documentModel = ModelFactory.createDefaultModel();
		documentModel.setNsPrefixes(collectionModel.getNsPrefixMap());
		ResIterator subjects = collectionModel.listSubjectsWithProperty(DKTNIF.DocumentName, documentName);
		while(subjects.hasNext()){
			Resource sub = subjects.next();
			StmtIterator iter = sub.listProperties();
			documentModel.add(iter);
			collectionModel.remove(iter);
		}
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
