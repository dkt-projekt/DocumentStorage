package de.dkt.eservices.edocumentstorage.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.persistence.dao.DocumentDAO;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.Document.Status;
import eu.freme.common.persistence.repository.DocumentRepository;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DocumentProcessor implements Runnable {

	@Autowired
	DocumentDAO dao;

	@Autowired
	DocumentProcessorService processorService;

	@Autowired
	NifConverterService nifConverterService;
	
	@Autowired
	DocumentRepository documentRepository;
	
	Logger logger = Logger.getLogger(DocumentProcessor.class);
	
	int id;
	static int idCounter=0;

	// TODO make endpoint a config parameter
	String pipelineApiEndpoint = "http://localhost:8099/pipelining/chain";

	boolean running = true;
	
	public DocumentProcessor(){
		this.id = idCounter++;
	}
	
	private Document fetchNextDocument(){
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
	
	private String convertToTurtle(Document doc){
		String turtle = null;
		try {
			turtle = nifConverterService.convertToTurtle(doc);
		} catch (Exception e) {
			logger.error("Cannot convert file", e);
			dao.setErrorState(doc, e.getMessage());
			return null;
		}
		return turtle;
	}
	
	private boolean addUriToDoc(Document doc, String turtle){
		String resourceUri = null;
		try {
			resourceUri = nifConverterService.getResourceUri(turtle);
			doc.setDocumentUri(resourceUri);
			documentRepository.save(doc);
		} catch (Exception e1) {
			logger.error("addUriToDoc failed", e1);
			dao.setErrorState(doc, e1.getMessage());
			return false;
		}
		return resourceUri != null;
	}
	
	public boolean executePipeline(Document doc, String turtle){
		try {

			// construct pipeline
			JSONArray pipeline = processorService.getPipeline();
			pipeline.getJSONObject(0).put("body", turtle);

			// execute http request
			HttpResponse<String> response = Unirest
					.post(pipelineApiEndpoint)
					.header("Content-Type", "application/json")
					.body(pipeline.toString())
					.asString();

			if (response.getStatus() != 200) {
				dao.setErrorState(doc, response.getBody());
				logger.error("document processor sets state of \""
						+ doc.getFilename() + "\" to error:\n"
						+ response.getBody());
				return false;
			}
			logger.debug("document processor #" + id + " finished processing file \""
					+ doc.getFilename() + "\"");
			return true;
		} catch (UnirestException e) {
			logger.error("cannot read file", e);
			dao.setErrorState(doc, e.getMessage());
			return false;
		}
	}

	@Override
	public void run() {

		while (running) {
			logger.debug("start document processor #" + id);
			
			// fetch document
			Document doc = fetchNextDocument();
			if( doc == null ){
				continue;
			}
			
			logger.debug("document processor #" + id + " starts processing file \""
					+ doc.getFilename() + "\"");
			
			// convert to turtle
			String turtle = convertToTurtle(doc);
			if( turtle == null ){
				continue;
			}
			
			// extract resource name and store as file
			if( !addUriToDoc(doc, turtle)){
				continue;
			}

			// execute pipeline
			if( executePipeline(doc, turtle) ){
				doc.setLastUpdate(new Date());
				doc.setStatus(Status.PROCESSED);
				documentRepository.save(doc);
			}
		}
	}
}
