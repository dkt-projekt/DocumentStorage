package de.dkt.eservices.edocumentstorage.collectionanalysis.exception;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
public class SparqlQueryDoesNotExistException extends FREMEHttpException {
	public SparqlQueryDoesNotExistException(){
		super("No sparql query with the given name exists", HttpStatus.BAD_REQUEST);
	}
}
