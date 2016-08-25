package de.dkt.eservices.edocumentstorage.service;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Service that coordinates the processing of the pipeline.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class DocumentProcessorService implements ApplicationContextAware {

	/**
	 * Number of threads to process the pipeline in parallel
	 */
	int numThreads = 2;

	/**
	 * Stores the pipeline for processing all documents.
	 */
	String pipeline;

	/**
	 * The classpath to the pipeline file.
	 */
	String pipelineFile = "pipeline.json";

	Logger logger = Logger.getLogger(DocumentProcessorService.class);

	/**
	 * These threads either sleep or process the pipeline.
	 */
	Thread[] workerThreads;

	ApplicationContext appContext;

	@PostConstruct
	public void init() throws IOException {

		// read pipeline definition file
		try {
			InputStream in = this.getClass().getClassLoader()
                    .getResourceAsStream(pipelineFile);
			pipeline = new String(IOUtils.toByteArray(in));
		} catch (IOException e) {
			logger.error("failed to read pipeline definition file \""
					+ pipelineFile + "\"", e);
			throw e;
		}

		// construct worker threads
		workerThreads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			SingleDocumentProcessor dp = appContext.getBean(SingleDocumentProcessor.class);
			Thread t = new Thread(dp);
			workerThreads[i] = t;
			t.start();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.appContext = applicationContext;
	}

	public JSONArray getPipeline() {
		return new JSONArray(pipeline);
	}

	/**
	 * Call this whenever new documents are added to the queue to start
	 * processing.
	 * 
	 */
	public void wakeupWorkers() {
		synchronized(this){
			this.notifyAll();			
		}
	}
}
