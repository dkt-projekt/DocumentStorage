package de.dkt.eservices.edocumentstorage;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.persistence.repository.DocumentCollectionRepository;
import eu.freme.common.persistence.repository.DocumentRepository;
import eu.freme.common.starter.FREMEStarter;

public class EDocumentStorageRestTest {

	@Test
	public void test() throws Exception {

		ApplicationContext appContext = FREMEStarter
				.startPackageFromClasspath("spring-configurations/edocumentstorage.xml");

		File file = File.createTempFile("dkt", "tmp");
		FileWriter fw = new FileWriter(file);
		fw.write("hello world");
		fw.close();

		String url = "http://localhost:8099/document-storage/my-collection";
		HttpResponse<String> response = Unirest.post(url)
				.queryString("fileName", "my-file.txt").field("file", file)
				.asString();
		assertTrue(response.getStatus() == 200);
		
		response = Unirest.post(url)
				.queryString("fileName", "my-other-file.txt").field("file", file)
				.asString();
		assertTrue(response.getStatus() == 200);

		HttpResponse<JsonNode> jsonResponse = Unirest.get(url).asJson();
		assertTrue(jsonResponse.getBody().getArray().length() == 2);
		
		DocumentRepository dr = appContext.getBean(DocumentRepository.class);
		DocumentCollectionRepository dcr = appContext.getBean(DocumentCollectionRepository.class);
		dr.deleteAll();
		dcr.deleteAll();
	}
}
