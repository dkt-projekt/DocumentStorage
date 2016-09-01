package de.dkt.eservices.edocumentstorage.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service to talk to the Create / Read / Update / Delete endpoint of the
 * Virtuoso Triple Store.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class SparqlCrudService {

	/**
	 * Username to authenticate with crudApiEndpoint
	 */

	@Value("${dkt.storage.virtuoso-username}")
	String username;

	/**
	 * Password to authenticate with crudApiEndpoint
	 */
	@Value("${dkt.storage.virtuoso-password}")
	String password;

	/**
	 * Virtuosos crudApiEndpoint
	 */
	@Value("${dkt.storage.virtuoso-crud-endpoint}")
	String crudApiEndpoint;

	private CloseableHttpClient getHttpClient() {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
				AuthScope.ANY_PORT), new UsernamePasswordCredentials(username,
				password));
		return HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
	}

	/**
	 * Write turtle to given graph. Return true in case of success, else false.
	 * 
	 * @param graphUri
	 * @param turtle
	 * @throws IOException
	 */
	public boolean addDataToStore(String graphUri, String turtle)
			throws IOException {

		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		boolean ok = false;
		try {
			httpclient = getHttpClient();
			String uri = crudApiEndpoint + "?graph-uri="
					+ URLEncoder.encode(graphUri, "utf-8");
			HttpPost request = new HttpPost(uri);

			request.addHeader("Content-Type", "text/turtle");

			HttpEntity entity = new StringEntity(turtle);
			request.setEntity(entity);

			response = httpclient.execute(request);
			StatusLine sl = response.getStatusLine();
			ok = sl.getStatusCode() == 200 || sl.getStatusCode() == 201;
		} finally {
			if (response != null) {
				response.close();
			}
			if (httpclient != null) {
				httpclient.close();
			}
		}

		return ok;
	}
}
