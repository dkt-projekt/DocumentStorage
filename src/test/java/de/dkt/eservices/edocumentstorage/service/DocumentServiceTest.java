package de.dkt.eservices.edocumentstorage.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.io.Files;

import de.dkt.eservices.edocumentstorage.DocumentStorageConfig;
import eu.freme.common.FREMECommonConfig;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DocumentStorageConfig.class, FREMECommonConfig.class})
public class DocumentServiceTest {

	@Autowired
	DocumentService documentService;

	@Autowired
	DocumentStorageConfig documentStorageConfig;
	
	@Autowired
	DocumentCollectionService documentCollectionService;
	
	@Autowired
	DocumentRepository documentRepository;
	
	@Autowired
	DocumentCollectionRepository documentCollectionRepository;
	
	@Test
	@Transactional
	public void testStorage() throws Exception{
		
		// init storage dir
		File tempDir = Files.createTempDir();
		documentStorageConfig.setDocumentStorageLocationStr(tempDir.getAbsolutePath());
		documentStorageConfig.postConstruct();
		
		// create a file
		String str = "hello world";
		ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
		
		// create collection and document
		DocumentCollection collection = documentCollectionService.createCollection("dummy-collection");
		Document doc = documentService.addFileToCollection(bais, "my-temp.file", collection);
		
		File[] list = documentCollectionService.getCollectionStorageDirectory(collection).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});
		
		assertTrue(list.length == 1);
		assertTrue(doc.getId() != null);
		
		documentRepository.deleteAll();
		documentCollectionRepository.deleteAll();
	}
}
