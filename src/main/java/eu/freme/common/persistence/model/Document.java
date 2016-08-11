package eu.freme.common.persistence.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
public class Document {

	public enum Status{
		NOT_PROCESSED,
		PROCESSED,
		ERROR
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Integer id;
	
	String filename;
	String path;
	Status status;
	String errorMessage;
	String documentUri;
	Date uploadTime;
	
	@ManyToOne
	DocumentCollection collection;
	
	public Document(){
	}
	
	public Document(Integer id, String filename, String path, Status status,
			String errorMessage, String documentUri, Date uploadTime) {
		super();
		this.id = id;
		this.filename = filename;
		this.path = path;
		this.status = status;
		this.errorMessage = errorMessage;
		this.documentUri = documentUri;
		this.uploadTime = uploadTime;
	}
	
	public String getFilename() {
		return filename;
	}
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getDocumentUri() {
		return documentUri;
	}
	public void setDocumentUri(String documentUri) {
		this.documentUri = documentUri;
	}
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}

	public DocumentCollection getCollection() {
		return collection;
	}

	public void setCollection(DocumentCollection collection) {
		this.collection = collection;
	}
	
	
}
