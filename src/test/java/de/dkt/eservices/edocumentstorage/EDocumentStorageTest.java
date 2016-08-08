package de.dkt.eservices.edocumentstorage;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.ValidationHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EDocumentStorageTest {

	TestHelper testHelper;
	ValidationHelper validationHelper;

	@Before
	public void setup() {
		ApplicationContext context = IntegrationTestSetup.getContext(TestConstants.pathToPackage);
		testHelper = context.getBean(TestHelper.class);
		validationHelper = context.getBean(ValidationHelper.class);
	}
	
	private HttpRequestWithBody DocumentStorageBaseRequestPost(String urlPart) {
		String url = testHelper.getAPIBaseUrl() + "/e-documentstorage/"+urlPart;
		Unirest.setTimeouts(10000, 10000000);
		return Unirest.post(url);
	}

	private GetRequest DocumentStorageBaseRequest(String urlPart) {
		String url = testHelper.getAPIBaseUrl() + "/e-documentstorage/"+urlPart;
		Unirest.setTimeouts(10000, 10000000);
		return Unirest.get(url);
	}

	private HttpRequestWithBody DocumentStorageBaseRequestDelete(String urlPart) {
		String url = testHelper.getAPIBaseUrl() + "/e-documentstorage/"+urlPart;
		Unirest.setTimeouts(10000, 10000000);
		return Unirest.delete(url);
	}

	@Test
	public void test1_CreateCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestPost("collection/testcollection21")
				.queryString("user", "dkt-projekt")
				.queryString("private", false)
				.queryString("users", "").asString();

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		
//		validationHelper.validateNIFResponse(response,
//				RDFConstants.RDFSerialization.TURTLE);
//
//		String data = FileUtils.readFileToString(new File("src/test/resources/rdftest/e-terminology/example1.ttl"));
////		String data = FileUtils.readFileToString(new File("src/test/resources/rdftest/e-translate/data.turtle"));
//		response = baseRequest().header("Content-Type", "text/turtle")
//				.body(data).asString();
//		validationHelper.validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
//
//		assertTrue(response.getStatus() == 200);
//		assertTrue(response.getBody().length() > 0);
//
//		response = baseRequest()
//				.queryString("informat", "text").queryString("outformat", "turtle").body("hello world")
//				.asString();
//		validationHelper.validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
//
//		assertTrue(response.getStatus() == 200);
//		assertTrue(response.getBody().length() > 0);

	}

	@Test
	public void test2_GetCollectionOverview() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection21/overview")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}
	
	@Test
	public void test3_GetCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection21/overview")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void test4_DeleteCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestDelete("collection/testcollection21")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void test5_AddDocumentToCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestPost("collection/testcollection21/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("input", "test text to put into the document to the collection")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void test6_1_GetDocumentContent() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection21/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("contentType", "nif")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void test6_2_GetDocumentContent() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection21/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("contentType", "other")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void test7_DeleteDocumentFromCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestDelete("collection/testcollection21/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("contentType", "other")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

}
