package de.dkt.eservices.edocumentstorage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.dkt.eservices.edocumentstorage.DocumentStorageConfig;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.DocumentRepository;

/**
 * Functionality around documents.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class DocumentService {

	@Autowired
	DocumentStorageConfig documentStorageConfig;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	DocumentCollectionService documentCollectionService;

	Logger logger = Logger.getLogger(DocumentService.class);

	/**
	 * Add a file to a collection. Moves file to storage and inserts file into
	 * database. Further it sets the documents status to NOT_PROCESSED.
	 * 
	 * @param file
	 * @param documentCollection
	 */
	@Transactional
	public Document addFileToCollection(InputStream inputStream,
			String fileName, DocumentCollection documentCollection, String pipeline)
			throws IOException {

		Document doc = new Document();
		doc.setPath(fileName);
		doc.setFilename(new File(fileName).getName());
		doc.setCollection(documentCollection);
		doc.setStatus(Document.Status.NOT_PROCESSED);
		doc.setUploadTime(new Date());
		doc.setPipeline(pipeline);
		doc = documentRepository.save(doc);

		File target = getDocumentLocation(doc);
		logger.debug("copy file " + fileName + " to " + target.getAbsolutePath());

		FileOutputStream fos = new FileOutputStream(target);
		IOUtils.copy(inputStream, fos);
		fos.close();

		return doc;
	}

	/**
	 * Get the path to the document on the file system
	 * 
	 * @param doc
	 * @return
	 */
	public File getDocumentLocation(Document doc) {
		return new File(
				documentCollectionService.getCollectionStorageDirectory(doc
						.getCollection()) + "/" + doc.getId());
	}
}
