package de.dkt.eservices.edocumentstorage.exception;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
public class DocumentCollectionExistsException extends FREMEHttpException{

	public DocumentCollectionExistsException(){
		super("A document collection with the given name already exists", HttpStatus.BAD_REQUEST);
	}
}
