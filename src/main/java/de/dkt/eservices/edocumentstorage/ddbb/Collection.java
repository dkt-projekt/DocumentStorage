package de.dkt.eservices.edocumentstorage.ddbb;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

public class Collection {

//	protected Integer id;
	protected String collectionName;
	protected boolean priv;
	protected List<String> users;
	protected List<String> documents;

	protected String filePath;

	public Collection() {
		super();
	}

	public Collection(String collectionName, boolean priv, List<String> users, List<String> documents,
			String filePath) {
		super();
		this.collectionName = collectionName;
		this.priv = priv;
		this.users = users;
		this.documents = documents;
		this.filePath = filePath;
	}

	public Collection(JSONObject collec) {
		super();
		this.collectionName = collec.getString("collectionName");
		this.priv = collec.getBoolean("priv");
		this.filePath = collec.getString("filePath");

		users = new LinkedList<String>();
		JSONObject jsonUsers = collec.getJSONObject("users");
		Set<String> usersKeys = jsonUsers.keySet();
		for (String uk : usersKeys) {
			users.add(jsonUsers.getString(uk));
		}
		
		documents = new LinkedList<String>();
		JSONObject jsonDocuments = collec.getJSONObject("documents");
		Set<String> docsKeys = jsonDocuments.keySet();
		for (String dk : docsKeys) {
			documents.add(jsonDocuments.getString(dk));
		}
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public boolean isPriv() {
		return priv;
	}

	public void setPriv(boolean priv) {
		this.priv = priv;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getDocuments() {
		return documents;
	}

	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public JSONObject getJSONObject(){
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("collectionName", collectionName);
		resultJSON.put("private", priv);
		int userCounter = 1;
		JSONObject usersJSON = new JSONObject();
		for (String s : users) {
			usersJSON.put(userCounter+"", s);
			userCounter++;
		}
		resultJSON.put("users", usersJSON);
		int docsCounter = 1;
		JSONObject documentsJSON = new JSONObject();
		for (String s : documents) {
			documentsJSON.put(docsCounter+"", s);
			docsCounter++;
		}
		resultJSON.put("documents", documentsJSON);
		resultJSON.put("filePath", filePath);
		return resultJSON;
	}

	public void addDocument(String documentName) {
		documents.add(documentName);
	}

}
