package de.dkt.eservices.edocumentstorage.service;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service to talk to the Create / Read / Update / Delete endpoint of the
 * Virtuoso Triple Store.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class TriplestoreService {

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
	
	Logger logger = Logger.getLogger(TriplestoreService.class);

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

			request.addHeader("Content-Type", "text/turtle; charset=utf-8");

			HttpEntity entity = new StringEntity(turtle, "utf-8");
			request.setEntity(entity);

			response = httpclient.execute(request);
			StatusLine sl = response.getStatusLine();
			ok = sl.getStatusCode() == 200 || sl.getStatusCode() == 201;
			
			if(!ok){
				logger.error("Triplestore returned status \"" + sl.getStatusCode() + "\" when trying to write data to it.");
			}
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

	/**
	 * Delete a whole graph from the triple store.
	 * 
	 * @param graphUri
	 * @return
	 * @throws IOException
	 */
	public boolean deleteGraph(String graphUri) throws IOException {
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		boolean ok = false;
		try {
			httpclient = getHttpClient();
			String uri = crudApiEndpoint + "?graph-uri="
					+ URLEncoder.encode(graphUri, "utf-8");
			HttpDelete request = new HttpDelete(uri);
			response = httpclient.execute(request);
			StatusLine sl = response.getStatusLine();
			ok = sl.getStatusCode() == 200 || sl.getStatusCode() == 201;
		} finally {
			httpclient.close();
		}
		return ok;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCrudApiEndpoint() {
		return crudApiEndpoint;
	}

	public void setCrudApiEndpoint(String crudApiEndpoint) {
		this.crudApiEndpoint = crudApiEndpoint;
	}

}
