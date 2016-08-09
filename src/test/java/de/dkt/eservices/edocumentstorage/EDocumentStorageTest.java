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

import com.hp.hpl.jena.rdf.model.Model;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import de.dkt.common.niftools.NIFReader;
import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.ValidationHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;

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
		HttpResponse<String> response = DocumentStorageBaseRequestPost("collection/testcollection22")
				.queryString("user", "dkt-projekt")
				.queryString("private", false)
				.queryString("users", "").asString();

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertTrue(response.getBody().contains("has been successfully created"));
//		System.out.println("BBOODDYY: "+response.getBody());
		
//		validationHelper.validateNIFResponse(response,
//				RDFConstants.RDFSerialization.TURTLE);
//		String data = FileUtils.readFileToString(new File("src/test/resources/rdftest/e-terminology/example1.ttl"));
//
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
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection22/overview")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = "{\"private\":false,\"documents\":{},\"filePath\":\"URL/testcollection22.cfe\",\"users\":{\"1\":\"dkt-projekt\"},\"collectionName\":\"testcollection22\"}";
		Assert.assertEquals(expected, response.getBody().replaceFirst("/(.*)/", "URL/"));
//		System.out.println("BBOODDYY GETO: "+response.getBody());

	}
	
	@Test
	public void test3_GetCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection22")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" + 
"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" + 
"\n" + 
"<http://dkt.dfki.de/storage/collection/testcollection22>\n"+
"        a       nif:ContextCollection .\n";
		Assert.assertEquals(expected, response.getBody());
		//System.out.println("BBOODDYY GET: "+response.getBody());
	}

	@Test
	public void test4_DeleteCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestDelete("collection/testcollection22")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" + 
"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" + 
"\n" + 
"<http://dkt.dfki.de/storage/collection/testcollection22>\n"+
"        a       nif:ContextCollection .\n";
		Assert.assertEquals(expected, response.getBody());
//		System.out.println("BBOODDYY DELETE: "+response.getBody());
	}

	@Test
	public void test5_1_CreateCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestPost("collection/testcollection33")
				.queryString("user", "dkt-projekt")
				.queryString("private", false)
				.queryString("users", "").asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertTrue(response.getBody().contains("has been successfully created"));
	}
	
	@Test
	public void test5_AddDocumentToCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestPost("collection/testcollection33/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("input", "test text to put into the document to the collection")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertTrue(response.getBody().contains("has been successfully created"));
//		System.out.println("-------------------\nBBOODDYY ADD: "+response.getBody()+"\n-------------------");
	}

	@Test
	public void test6_1_GetDocumentContent() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection33/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("contentType", "nif")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"+
"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n"+
"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n"+
"\n"+
"<http://dkt.dfki.de/storage/collection/testcollection33/document/docName1#char=0,53>\n"+
"        a               nif:Context , nif:String , nif:RFC5147String ;\n"+
"        <http://dkt.dfki.de/ontologies/nif#DocumentName>\n"+
"                \"docName1\"^^xsd:string ;\n"+
"        <http://dkt.dfki.de/ontologies/nif#DocumentPath>\n"+
"                \""+DocumentStorage.storageDirectory+"ds_plaintext_51143.218932217597879828632919911584.txt\"^^xsd:string ;\n"+
"        nif:beginIndex  \"0\"^^xsd:nonNegativeInteger ;\n"+
"        nif:endIndex    \"53\"^^xsd:nonNegativeInteger ;\n"+
"        nif:isString    \"test text to put into the document to the collection\\n\"^^xsd:string .\n";
		Assert.assertEquals(expected.replaceFirst("plaintext_(.*).txt", "XXXX"), response.getBody().replaceFirst("plaintext_(.*).txt", "XXXX"));
//		System.out.println("-------------------\nBBOODDYY GET NIF: "+response.getBody()+"\n-------------------");
	}

	@Test
	public void test6_2_GetDocumentContent() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequest("collection/testcollection33/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("contentType", "other")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = DocumentStorage.storageDirectory+"ds_plaintext_26228.482317365114506372550020066922.txt";
		Assert.assertEquals(expected.replaceFirst("plaintext_(.*).txt", "XXXX"), response.getBody().replaceFirst("plaintext_(.*).txt", "XXXX"));
//		System.out.println("-------------------\nBBOODDYY GET CONTENT: "+response.getBody()+"\n-------------------");
	}

	@Test
	public void test7_DeleteDocumentFromCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestDelete("collection/testcollection33/document/docName1")
				.queryString("user", "dkt-projekt")
				.queryString("contentType", "other")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"+
"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n"+
"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n"+
"\n"+
"<http://dkt.dfki.de/storage/collection/testcollection33/document/docName1#char=0,53>\n"+
"        a               nif:Context , nif:String , nif:RFC5147String ;\n"+
"        <http://dkt.dfki.de/ontologies/nif#DocumentName>\n"+
"                \"docName1\"^^xsd:string ;\n"+
"        <http://dkt.dfki.de/ontologies/nif#DocumentPath>\n"+
"                \""+DocumentStorage.storageDirectory+"ds_plaintext_51143.218932217597879828632919911584.txt\"^^xsd:string ;\n"+
"        nif:beginIndex  \"0\"^^xsd:nonNegativeInteger ;\n"+
"        nif:endIndex    \"53\"^^xsd:nonNegativeInteger ;\n"+
"        nif:isString    \"test text to put into the document to the collection\\n\"^^xsd:string .\n";
		Assert.assertEquals(expected.replaceFirst("plaintext_(.*).txt", "XXXX"), response.getBody().replaceFirst("plaintext_(.*).txt", "XXXX"));
//		System.out.println("-------------------\nBBOODDYY DELETE: "+response.getBody()+"\n-------------------");
	}

	@Test
	public void test9_1_DeleteCollection() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = DocumentStorageBaseRequestDelete("collection/testcollection33")
				.queryString("user", "dkt-projekt")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		String expected = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" + 
"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" + 
"\n" + 
"<http://dkt.dfki.de/storage/collection/testcollection33>\n"+
"        a       nif:ContextCollection .\n";
		Assert.assertEquals(expected, response.getBody());
//		System.out.println("BBOODDYY DELETE: "+response.getBody());
	}


}
