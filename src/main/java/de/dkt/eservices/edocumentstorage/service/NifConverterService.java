package de.dkt.eservices.edocumentstorage.service;

import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.persistence.model.Document;

/**
 * This service calls the nif converter service to convert any document to
 * turtle.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class NifConverterService {

	// @Value("#{document-storage.nif-converter-url ?: @null}")
	String nifConverterUrl = "http://api-dev.freme-project.eu/current/toolbox/nif-converter";

	String prefix = "http://digitale-kuratierung.de/ns/";

	// @Value("#{server.port ?: 8080}")
	// String port;

	@PostConstruct
	public void init() {

	}

	@Autowired
	RDFConversionService rdfConverter;

	/**
	 * Convert a document to a turtle file using the nif-converter API
	 * 
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public String convertToTurtle(Document doc) throws Exception {
		Model model = ModelFactory.createDefaultModel();
		String thisPrefix = prefix
				+ URLEncoder.encode(doc.getPath(), "utf-8");
		rdfConverter.plaintextToRDF(model, "hello world", null, thisPrefix);
		return rdfConverter.serializeRDF(model,
				RDFConstants.RDFSerialization.TURTLE);
	}

	/**
	 * Extract the resource URI of a turtle file
	 * 
	 * @param ttl
	 * @return
	 * @throws Exception
	 */
	public String getResourceUri(String ttl) throws Exception {
		Model model = rdfConverter.unserializeRDF(ttl,
				RDFConstants.RDFSerialization.TURTLE);
		Property type = model
				.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Resource context = model
				.getProperty(RDFConstants.nifPrefix + "Context");
		ResIterator itr = model.listResourcesWithProperty(type, context);
		Resource r = itr.next();
		itr.close();

		if (r == null) {
			return null;
		} else {
			return r.getURI();
		}
	}
}
