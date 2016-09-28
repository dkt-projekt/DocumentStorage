package de.dkt.eservices.edocumentstorage;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import static org.junit.Assert.assertTrue;
import eu.freme.common.persistence.model.Document;
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
	public void fullProcessTest() throws UnirestException, InterruptedException,
			IOException {

		ConfigurableApplicationContext appContext = FREMEStarter
				.startPackageFromClasspath("storage-test-package.xml");

		String port = appContext.getEnvironment().getProperty("server.port");
		String baseUrl = "http://localhost:" + port;

		String storageUrl = baseUrl
				+ "/document-storage/collections/my-collection";
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
		Thread.sleep(10000);

		HttpResponse<JsonNode> jsonResponse = Unirest.get(
				storageUrl + "/documents").asJson();
		assertTrue(jsonResponse.getStatus() == 200);
		JSONArray array = jsonResponse.getBody().getArray();

		System.out.println(array.toString(1));
		
		assertTrue(array.length() == 8);

		// check if all files are in the triple store
		String turtleResponse = Unirest.get(tripleStoreUrl).asString()
				.getBody();
		for (int i = 0; i < array.length(); i++) {
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

	@Test
	public void testCustomPipeline() throws UnirestException, IOException, InterruptedException {

		ConfigurableApplicationContext appContext = FREMEStarter
				.startPackageFromClasspath("storage-test-package.xml");

		String port = appContext.getEnvironment().getProperty("server.port");
		String baseUrl = "http://localhost:" + port;

		String storageUrl = baseUrl
				+ "/document-storage/collections/my-collection";

		// add pipeline to server via pipelining api

		// get authentication token
		String username = appContext.getEnvironment().getProperty(
				"admin.username");
		String password = appContext.getEnvironment().getProperty(
				"admin.password");
		HttpResponse<String> response = Unirest.post(baseUrl + "/authenticate")
				.header("X-Auth-Username", username)
				.header("X-Auth-Password", password).asString();
		assertTrue(response.getStatus() == 200);
		JSONObject json = new JSONObject(response.getBody());
		String token = json.getString("token");

		String pipelineStr = FileUtils.readFileToString(new File(
				"src/test/resources/pipeline.json"));
		response = Unirest.post(baseUrl + "/pipelining/templates")
				.queryString("label", "test").queryString("token", token)
				.header("Content-Type", "application/json").body(pipelineStr)
				.asString();

		json = new JSONObject(response.getBody());

		assertTrue(response.getStatus() == 200);
		Integer pipelineId = json.getInt("id");

		// add file to this pipeline
		response = Unirest.post(storageUrl)
				.header("Content-Type", "text/plain")
				.queryString("fileName", "test.txt")
				.queryString("pipeline", pipelineId)
				.body("hello world".getBytes()).asString();
		assertTrue(response.getStatus() == 200);
		
		Thread.sleep(5000);
		
		HttpResponse<JsonNode> jsonResponse = Unirest.get(
				storageUrl + "/documents").asJson();
		assertTrue(jsonResponse.getStatus() == 200);
		JSONArray array = jsonResponse.getBody().getArray();
		
		assertTrue(array.length() == 1);
		String state = array.getJSONObject(0).getString("status");
		assertTrue(state.equals(Document.Status.PROCESSED.toString()));
		
		appContext.close();
	}
}
