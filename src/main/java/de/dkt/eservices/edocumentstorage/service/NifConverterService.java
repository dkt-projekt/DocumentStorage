package de.dkt.eservices.edocumentstorage.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.persistence.dao.DocumentDAO;
import eu.freme.common.persistence.model.Document;

/**
 * This service calls the nif converter service to convert any document to
 * turtle.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class NifConverterService {

	String nifConverterUrl;

	String prefix = "http://digitale-kuratierung.de/ns/";

	@Value("${server.port:8080}")
	String port;
	
	@Autowired
	DocumentDAO documentDao;

	@PostConstruct
	public void init() {
//		nifConverterUrl = "http://localhost:" + port + "/toolbox/nif-converter";
		nifConverterUrl = "https://dev.digitale-kuratierung.de/api/toolbox/nif-converter";
	}

	@Autowired
	RDFConversionService rdfConverter;
	
	@Autowired
	DocumentService documentService;
	
	Logger logger = Logger.getLogger(NifConverterService.class);

	/**
	 * Convert a document to a turtle file using the nif-converter API
	 * 
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public String convertToTurtle(Document doc) throws Exception {
		String thisPrefix = prefix + URLEncoder.encode(doc.getPath(), "utf-8");
		Model output = NIFWriter.initializeOutputModel();
		String fileName = doc.getPath() + File.separator + doc.getFilename();
		InputStream is = new FileInputStream(fileName);
		String content = IOUtils.toString(is);
		NIFWriter.addInitialString(output, content, thisPrefix);
		return NIFReader.model2String(output, RDFSerialization.TURTLE);
//		HttpResponse<String> response = Unirest
//				.post(nifConverterUrl)
//				.queryString("informat", "TIKAFile")
//				.queryString("prefix", thisPrefix)
//				.header("Accept", "text/turtle")
//				.field("inputFile", documentService.getDocumentLocation(doc))
//				.asString();
//
//		if( response.getStatus() == 200 ){
//			String ttl = response.getBody().toString();
//			return ttl;
//		} else{
//			documentDao.setErrorState(doc, response.toString());
//			throw new RuntimeException(response.toString());
//		}
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
