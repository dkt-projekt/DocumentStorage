package de.dkt.eservices.edocumentstorage;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import eu.freme.common.starter.FREMEStarter;

/**
 * Test upload of a single file, upload of zip file, retrieval of these files
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class EDocumentStorageRestTest {

	static ConfigurableApplicationContext appContext = null;
	static String url;

	@BeforeClass
	public static void setup() {
		appContext = FREMEStarter
				.startPackageFromClasspath("spring-configurations/edocumentstorage.xml");

		String port = appContext.getEnvironment().getProperty("server.port");
		url = "http://localhost:" + port
				+ "/document-storage/collections/my-collection";

		EDocumentStorageRestTest.clearDb();
	}

	@AfterClass
	public static void teardown() {
		appContext.close();
	}

	@Test
	public void testSingleUpload() throws Exception {

		String str = "hello world";

		// create collection
		HttpResponse<String> response = Unirest.post(url).asString();
		assertTrue(response.getStatus() == 200);

		// upload file
		response = Unirest.post(url + "/documents").queryString("fileName", "my-file.txt")
				.header("Content-Type", "text/plain").body(str.getBytes())
				.asString();
		assertTrue(response.getStatus() == 200);

		response = Unirest.post(url + "/documents")
				.queryString("fileName", "my-other-file.txt")
				.header("Content-Type", "text/plain").body(str.getBytes())
				.asString();
		assertTrue(response.getStatus() == 200);

		HttpResponse<JsonNode> jsonResponse = Unirest.get(url + "/documents")
				.asJson();
		assertTrue(jsonResponse.getBody().getArray().length() == 2);

		EDocumentStorageRestTest.clearDb();
	}

	// helper function to clear the database
	private static void clearDb() {
		DocumentRepository dr = appContext.getBean(DocumentRepository.class);
		DocumentCollectionRepository dcr = appContext
				.getBean(DocumentCollectionRepository.class);
		dr.deleteAll();
		dcr.deleteAll();
	}

	@Test
	public void testZipUpload() throws UnirestException, IOException {
		File file = new File("src/test/resources/pipeline.zip");
		byte[] data = FileUtils.readFileToByteArray(file);

		// create collection
		HttpResponse<String> response = Unirest.post(url).asString();
		assertTrue(response.getStatus() == 200);

		// upload file
		HttpResponse<String> responseStr = Unirest.post(url + "/documents")
				.header("Content-Type", "application/zip")
				.queryString("fileName", "file.zip").body(data).asString();
		assertTrue(responseStr.getStatus() == HttpStatus.OK.value());

		HttpResponse<JsonNode> jsonResponse = Unirest.get(url + "/documents")
				.asJson();
		assertTrue(jsonResponse.getBody().getArray().length() > 0);

		EDocumentStorageRestTest.clearDb();
	}
}
