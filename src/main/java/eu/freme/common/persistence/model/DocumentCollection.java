package eu.freme.common.persistence.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Entity
public class DocumentCollection {

	@Id
	String name;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "collection")
	@JsonIgnore
	List<Document> documents;

	Date creationTime;

	public DocumentCollection() {
	}

	public DocumentCollection(String name, List<Document> documents,
			Date creationTime) {
		super();
		this.name = name;
		this.documents = documents;
		this.creationTime = creationTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

}
