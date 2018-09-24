package de.dkt.eservices.edocumentstorage.collectionanalysis.exception;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
public class SparqlQueryExistsException extends FREMEHttpException{

	public SparqlQueryExistsException(){
		super("A sparql query with the given name already exists", HttpStatus.BAD_REQUEST);
	}
}
