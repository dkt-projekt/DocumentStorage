package eu.freme.broker.edocumentstorage.exceptions;

public class ExternalServiceFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExternalServiceFailedException(String message) {
		super(message);
	}
}
