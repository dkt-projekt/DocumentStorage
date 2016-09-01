package de.dkt.eservices.edocumentstorage;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import static org.junit.Assert.assertTrue;
import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import eu.freme.common.starter.FREMEStarter;

/**
 * Tests the whole process from adding a document, executing the pipeline to
 * writing to a mockup endpoint that is the triple store
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class FullProcessTest {

	@Test
	public void test() throws UnirestException, InterruptedException, IOException {

		ConfigurableApplicationContext appContext = FREMEStarter
				.startPackageFromClasspath("storage-test-package.xml");

		String port = appContext.getEnvironment().getProperty("server.port");
		String baseUrl = "http://localhost:" + port;

		String storageUrl = baseUrl + "/document-storage/my-collection";
		String tripleStoreUrl = baseUrl + "/test-endpoint";

		// add a file
		String str = new String("hello world");
		HttpResponse<String> response = Unirest.post(storageUrl)
				.header("Content-Type", "text/plain")
				.queryString("fileName", "test.txt").body(str.getBytes())
				.asString();
		assertTrue(response.getStatus() == 200);

		// wait for 5 seconds
		Thread.sleep(5000);

		// retrieve data from triple store and check for HELLO WORLD
		response = Unirest.get(tripleStoreUrl).asString();
		assertTrue(response.getBody().contains("HELLO WORLD"));

		// add zip
		byte[] data = FileUtils.readFileToByteArray(new File(
				"src/test/resources/pipeline.zip"));
		response = Unirest.post(storageUrl)
				.header("Content-Type", "application/zip")
				.queryString("fileName", "file.zip").body(data).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());

		// wait for 5 seconds
		Thread.sleep(5000);
		
		HttpResponse<JsonNode> jsonResponse = Unirest.get(storageUrl + "/documents").asJson();
		assertTrue(jsonResponse.getStatus()==200);
		JSONArray array = jsonResponse.getBody().getArray();
		
		assertTrue(array.length() == 8);
		
		// check if all files are in the triple store
		String turtleResponse = Unirest.get(tripleStoreUrl).asString().getBody();
		for( int i=0; i<array.length(); i++ ){
			String uri = array.getJSONObject(i).getString("documentUri");
			assertTrue(turtleResponse.contains(uri));
		}
		
		// clean up
		DocumentRepository dr = appContext.getBean(DocumentRepository.class);
		DocumentCollectionRepository dcr = appContext
				.getBean(DocumentCollectionRepository.class);
		dr.deleteAll();
		dcr.deleteAll();
		
		appContext.close();
	}
}
