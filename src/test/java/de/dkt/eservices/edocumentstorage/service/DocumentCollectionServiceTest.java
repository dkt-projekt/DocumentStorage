package de.dkt.eservices.edocumentstorage.service;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.dkt.eservices.edocumentstorage.DocumentStorageConfig;
import de.dkt.eservices.edocumentstorage.exception.DocumentCollectionExistsException;
import eu.freme.common.FREMECommonConfig;
import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DocumentStorageConfig.class, FREMECommonConfig.class})
public class DocumentCollectionServiceTest {

	@Autowired
	DocumentCollectionService documentCollectionService;
	
	@Autowired
	DocumentCollectionRepository documentCollectionRepository;
	
	@Autowired
	DocumentRepository documentRepository;
	
	@Autowired
	DataSource dataSource;
	
	@Test
	@Transactional
	public void basicTest() throws Exception{
		
		documentRepository.deleteAll();
		documentCollectionRepository.deleteAll();
		
		String name = "test";
		
		DocumentCollection dc = documentCollectionService.createCollection(name);
		assertTrue(documentCollectionRepository.count() == 1);
		
		try{
			documentCollectionService.createCollection(name);
			assertTrue(true);
		} catch( DocumentCollectionExistsException e){
			// all good
		}
		
		Document d1 = new Document();
		d1.setCollection(dc);
		documentRepository.save(d1);

		Document d2 = new Document();
		d2.setCollection(dc);
		documentRepository.save(d2);
		
		dc.getDocuments().add(d1);
		dc.getDocuments().add(d2);
		
		dc = documentCollectionRepository.findOneByName(name);
		assertTrue(dc.getDocuments().size()==2);
		
		documentRepository.deleteAll();
		documentCollectionRepository.deleteAll();
	}
	
	@Test
	public void validateFileNames(){
		assertTrue(documentCollectionService.validateCollectionName("abc"));
		assertTrue(documentCollectionService.validateCollectionName("ab1c"));
		assertTrue(documentCollectionService.validateCollectionName("aAc"));
		assertTrue(documentCollectionService.validateCollectionName("abc-"));
		assertFalse(documentCollectionService.validateCollectionName("abc*"));
		assertFalse(documentCollectionService.validateCollectionName("abc/"));
		assertFalse(documentCollectionService.validateCollectionName("a"));
		
	}
}
