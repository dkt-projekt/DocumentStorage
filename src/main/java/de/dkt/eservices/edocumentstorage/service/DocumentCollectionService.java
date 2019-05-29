package de.dkt.eservices.edocumentstorage.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.dkt.eservices.edocumentstorage.DocumentStorageConfig;
import de.dkt.eservices.edocumentstorage.exception.DocumentCollectionDoesNotExistException;
import de.dkt.eservices.edocumentstorage.exception.DocumentCollectionExistsException;
import de.dkt.eservices.edocumentstorage.exception.InvalidDocumentCollectionNameException;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.DocumentCollectionRepository;

/**
 * Functionality around document collections.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class DocumentCollectionService {

	@Autowired
	DocumentCollectionRepository documentCollectionRepository;

	@Autowired
	DocumentStorageConfig documentStorageConfig;

	@Autowired
	DocumentService documentService;

	@Autowired
	TriplestoreService tripleStoreCrudService;

	/**
	 * This variable describes the uri of the the graph in which the collection
	 * data is stored in the triple store. The collection name will be appended
	 * to the graph uri.
	 */
	String graphBaseUri = "http://digitale-kuratierung.de/ns/graphs/";

	Logger logger = Logger.getLogger(DocumentCollection.class);

	/**
	 * Create collection in database, create storage location
	 * 
	 * @param name
	 * @return
	 * @throws DocumentCollectionExistsException
	 */
	@Transactional
	public DocumentCollection createCollection(String name)
			throws DocumentCollectionExistsException {

		if (!validateCollectionName(name)) {
			throw new InvalidDocumentCollectionNameException();
		}

		if (documentCollectionRepository.findOneByName(name) != null) {
			throw new DocumentCollectionExistsException();
		}

		DocumentCollection dc = new DocumentCollection(name,
				new ArrayList<Document>(), new Date());
		dc = documentCollectionRepository.save(dc);

		File file = getCollectionStorageDirectory(dc);
		file.mkdirs();

		return dc;
	}

	/**
	 * Validate that the name of a collection has the valid format.
	 * 
	 * @param name
	 * @return
	 */
	public boolean validateCollectionName(String name) {
		return name != null && name.length() >= 3 && name.length() <= 16
				&& Pattern.matches("[a-zA-Z0-9\\-_]*", name);
	}

	/**
	 * Delete a collection. Currently deletes the collection from the database.
	 * Should delete its file and probably the data from the triple store also
	 * in a later version.
	 * 
	 * @param name
	 * @throws IOException
	 */
	@Transactional
	public void deleteCollection(DocumentCollection dc) throws IOException {

		String graphUri = getGraphUri(dc);
		
		tripleStoreCrudService.deleteGraph(graphUri);
		
		File dir = getCollectionStorageDirectory(dc);
		FileUtils.deleteDirectory(dir);
		
		documentCollectionRepository.delete(dc);
	}

	/**
	 * Get the storage location of a DocumentCollection
	 * 
	 * @param documentCollection
	 * @return
	 */
	public File getCollectionStorageDirectory(
			DocumentCollection documentCollection) {

		File storageDir = documentStorageConfig.getDocumentStorageLocation();
		try {
			String safeName = java.net.URLEncoder.encode(
					documentCollection.getName(), "UTF-8");
			String path = storageDir.getAbsolutePath() + "/" + safeName;

			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return new File(path);
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is an unknown encoding!?");
		}
	}

	/**
	 * Unpack zip file and add each to the collection.
	 * 
	 * @param dc
	 * @param inputFile
	 * @throws IOException
	 */
	@Transactional
	public void addZipFileToCollection(DocumentCollection dc, File inputFile, String pipeline)
			throws IOException {
		ArrayList<Document> writtenFiles = new ArrayList<Document>();

		ZipFile zipFile = null;
		try {

			zipFile = new ZipFile(inputFile);

			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e
					.hasMoreElements();) {
				ZipEntry entry = e.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				InputStream is = zipFile.getInputStream(entry);
				Document doc = documentService.addFileToCollection(is,
						entry.getName(), dc, pipeline);
				writtenFiles.add(doc);
			}

			zipFile.close();
			zipFile = null;
		} catch (Exception e) {
			for (Document doc : writtenFiles) {
				File file = documentService.getDocumentLocation(doc);
				file.delete();
			}

			throw e;
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}

		}
	}

	public String getGraphUri(DocumentCollection dc) {
		try {
			return graphBaseUri + URLEncoder.encode(dc.getName(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return null;
		}
	}
}
