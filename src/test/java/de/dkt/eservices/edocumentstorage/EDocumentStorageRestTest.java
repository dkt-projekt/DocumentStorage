package de.dkt.eservices.edocumentstorage;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import eu.freme.common.starter.FREMEStarter;

public class EDocumentStorageRestTest {

	ApplicationContext appContext = null;
	String url = "http://localhost:8099/document-storage/my-collection";

	@Before
	public void setup() {
		appContext = FREMEStarter
				.startPackageFromClasspath("spring-configurations/edocumentstorage.xml");
		this.clearDb();
	}

	@Test
	public void testSingleUpload() throws Exception {

		String str = "hello world";

		HttpResponse<String> response = Unirest.post(url)
				.queryString("fileName", "my-file.txt")
				.header("Content-Type", "text/plain")
				.body(str.getBytes()).asString();
		assertTrue(response.getStatus() == 200);
		
		response = Unirest.post(url)
				.queryString("fileName", "my-other-file.txt")
				.header("Content-Type", "text/plain")
				.body(str.getBytes()).asString();
		assertTrue(response.getStatus() == 200);

		HttpResponse<JsonNode> jsonResponse = Unirest.get(url).asJson();
		assertTrue(jsonResponse.getBody().getArray().length() == 2);

		this.clearDb();
	}

	// helper function to clear the database
	private void clearDb() {
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

		HttpResponse<String> responseStr = Unirest.post(url)
				.header("Content-Type", "application/zip")
				.queryString("fileName", "file.zip")
				.body(data)
				.asString();
		assertTrue(responseStr.getStatus() == HttpStatus.OK.value());

		HttpResponse<JsonNode> jsonResponse = Unirest.get(url).asJson();
		assertTrue(jsonResponse.getBody().getArray().length() == 7);

		this.clearDb();
	}
}
