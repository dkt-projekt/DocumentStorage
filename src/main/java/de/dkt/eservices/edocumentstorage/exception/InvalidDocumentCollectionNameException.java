package de.dkt.eservices.edocumentstorage.exception;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
public class InvalidDocumentCollectionNameException extends FREMEHttpException{

	public InvalidDocumentCollectionNameException(){
		super("A document collections name can only consist of characters, letters, dash or underscore. Its length must be between 3 and 16 characters.", HttpStatus.BAD_REQUEST);
	}
}
