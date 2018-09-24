package de.dkt.eservices.edocumentstorage.collectionanalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.dkt.eservices.edocumentstorage.collectionanalysis.exception.SparqlQueryDoesNotExistException;
import de.dkt.eservices.edocumentstorage.collectionanalysis.exception.SparqlQueryExistsException;
import eu.freme.common.persistence.model.CollectionAnalysis;
import eu.freme.common.persistence.model.DocumentCollection;
import eu.freme.common.persistence.repository.CollectionAnalysisRepository;

@Component
public class CollectionAnalysisService {

	
	Logger logger = Logger.getLogger(CollectionAnalysisService.class);

	@Autowired
	CollectionAnalysisRepository collectionAnalysisRepository;
	
	@Transactional
	public CollectionAnalysis createSparqlQuery(String name, String query){


		if (collectionAnalysisRepository.findOneByName(name) != null) {
			throw new SparqlQueryExistsException();
		}

		CollectionAnalysis sparqlQuery = new CollectionAnalysis(name, query);
		sparqlQuery = collectionAnalysisRepository.save(sparqlQuery);

		return sparqlQuery;
	}
	
	@Transactional
	public void deleteSparqlQuery(String name) {
		if (collectionAnalysisRepository.findOneByName(name) == null) {
			throw new SparqlQueryDoesNotExistException();
		}
		collectionAnalysisRepository.delete(name);
	}
	
	@Transactional
	public void updateSparqlQueryName(String oldName, String newName) {
		CollectionAnalysis ca = collectionAnalysisRepository.findOneByName(oldName);
		if (ca == null) {
			throw new SparqlQueryDoesNotExistException();
		}
		ca.setName(newName);
//		collectionAnalysisRepository.save(ca);
	}
	
	@Transactional
	public void updateSparqlQuery(String oldName, String newQuery) {
		CollectionAnalysis ca = collectionAnalysisRepository.findOneByName(oldName);
		if (ca == null) {
			throw new SparqlQueryDoesNotExistException();
		}
		ca.setQuery(newQuery);
//		collectionAnalysisRepository.save(ca);

	}
	
	@Transactional
	public String listSparqlQueryNames(){
		Iterator<CollectionAnalysis> itr = collectionAnalysisRepository.findAll().iterator();
		ArrayList<String> list = new ArrayList<String>();
		while( itr.hasNext() ){
			list.add(itr.next().getName());
		}
		JSONArray json = new JSONArray(list);
		return json.toString();
	}
}
