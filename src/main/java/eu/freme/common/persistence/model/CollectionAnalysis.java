package eu.freme.common.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table(name = "sparql_queries")
public class CollectionAnalysis {


	@Id
	@Lob
	private String name;
	
	@Lob
	private String query;

	public CollectionAnalysis(){
		
	}
	public CollectionAnalysis(String name, String query) {
		this.name=name;
		this.query=query;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}


}