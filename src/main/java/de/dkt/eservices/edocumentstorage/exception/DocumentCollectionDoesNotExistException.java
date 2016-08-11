package de.dkt.eservices.edocumentstorage.exception;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
public class DocumentCollectionDoesNotExistException extends FREMEHttpException{

	public DocumentCollectionDoesNotExistException(){
		super("A document collection with the given name does not exist", HttpStatus.BAD_REQUEST);
	}
}
