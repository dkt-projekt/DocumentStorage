package de.dkt.eservices.edocumentstorage.service;

import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.common.persistence.dao.DocumentDAO;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.Document.Status;
import eu.freme.common.persistence.repository.DocumentRepository;

/**
 * The DocumentProcessor coordinates processing of documents. It implements
 * Runnable, so usually there are multiple DocumentProcessors running in
 * parallel.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SingleDocumentProcessor implements Runnable {

	@Autowired
	DocumentDAO dao;

	@Autowired
	DocumentProcessorService processorService;

	@Autowired
	NifConverterService nifConverterService;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	TriplestoreService sparqlCrudService;

	@Autowired
	DocumentCollectionService documentCollectionService;

	Logger logger = Logger.getLogger(SingleDocumentProcessor.class);

	int id;
	static int idCounter = 0;

	@Value("${server.port:8080}")
	String port;

	/**
	 * url that is injected as $base-url$ in the pipeline
	 */
	@Value("${dkt.storage.pipeline.base-url:null}")
	String pipelineBaseUrl;
	
	boolean running = true;

	public SingleDocumentProcessor() {
		this.id = idCounter++;
	}

	private Document fetchNextDocument() {
		Document doc = null;
		synchronized (processorService) {
			doc = dao.fetchNextForProcessing();
			if (doc == null) {
				try {
					processorService.wait();
				} catch (InterruptedException e) {
					logger.error("interruped", e);
				}
				return null;
			}
		}
		return doc;
	}

	private String convertToTurtle(Document doc) {
		String turtle = null;
		try {
			turtle = nifConverterService.convertToTurtle(doc);
			logger.debug("Processor #" + id + " converted \""
					+ doc.getFilename() + "\" to turtle:\n" + turtle);
		} catch (Exception e) {
			logger.error("Cannot convert file", e);
			dao.setErrorState(doc, e.getMessage());
			return null;
		}
		return turtle;
	}

	private boolean addUriToDoc(Document doc, String turtle) {
		String resourceUri = null;
		try {
			resourceUri = nifConverterService.getResourceUri(turtle);
			logger.debug("Processor #" + id + " extracted resource uri \""
					+ resourceUri + "\" from \"" + doc.getFilename());
			doc.setDocumentUri(resourceUri);
			documentRepository.save(doc);
		} catch (Exception e1) {
			logger.error("addUriToDoc failed", e1);
			dao.setErrorState(doc, e1.getMessage());
			return false;
		}
		return resourceUri != null;
	}

	private String executePipeline(Document doc, String turtle) {
		try {

			HttpRequestWithBody request = null;
			String body = null;
			if (doc.getPipeline() == null) {
				throw new RuntimeException("Pipeline is not set");
			} else {
				// use specific pipeline
				request = Unirest.post(doc.getPipeline()).header("Content-Type","text/turtle");
				body = turtle;
				logger.debug("document processor #" + id + ": process document with specific pipeline\"");
			}

			// add pipeline parameters
			request.queryString("base-url", pipelineBaseUrl);
			request.queryString("collection-name", doc.getCollection()
					.getName());

			// execute http request
			HttpResponse<String> response = request.body(body).asString();

			if (response.getStatus() != 200) {
				dao.setErrorState(doc, response.getBody());
				logger.error("document processor sets state of \""
						+ doc.getFilename() + "\" to error:\n"
						+ response.getBody());
				return null;
			}

			logger.debug("document processor #" + id + " returned doc \""
					+ doc.getFilename() + "\" from pipeline, response:\n"
					+ response.getBody());
			return response.getBody();
		} catch (UnirestException e) {
			logger.error("error executing the pipeline", e);
			dao.setErrorState(doc, e.getMessage());
			return null;
		}
	}

	private boolean writeToTripleStore(Document doc, String enrichedTurtle) {
		String graphUri = documentCollectionService.getGraphUri(doc
				.getCollection());
		try {
			return sparqlCrudService.addDataToStore(graphUri, enrichedTurtle);
		} catch (Exception e) {
			logger.error(e);
			dao.setErrorState(doc, e.getMessage());
			return false;
		}
	}

	@Override
	public void run() {

		while (running) {
			Document doc = null;
			try {
				logger.debug("start document processor #" + id);

				// fetch document
				doc = fetchNextDocument();
				if (doc == null) {
					continue;
				}

				logger.debug("document processor #" + id
						+ " starts processing file \"" + doc.getFilename()
						+ "\"");

				// convert to turtle
				String turtle = convertToTurtle(doc);
				logger.debug("before\n" + turtle);
				if (turtle == null) {
					continue;
				}

				// extract resource name and store as file
				if (!addUriToDoc(doc, turtle)) {
					continue;
				}

				// execute pipeline
				String enrichedTurtle = null;
				if ((enrichedTurtle = executePipeline(doc, turtle)) == null) {
					continue;
				}

				// write result to triple store
				if (writeToTripleStore(doc, enrichedTurtle)) {
					logger.debug("document processor #" + id
							+ " finished processing file \""
							+ doc.getFilename() + "\"");
					doc.setLastUpdate(new Date());
					doc.setStatus(Status.PROCESSED);
					documentRepository.save(doc);
				} else{
					dao.setErrorState(doc, "Could not write document to the triple store.");
				}
			} catch (Exception e) {
				logger.error("An exception occured", e);
				if (doc != null) {
					dao.setErrorState(doc, e.getMessage());
				}
			}
		}

	}
}
