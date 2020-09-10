package ognjenj.charon.acct.exceptions;

public class ResponseValidationException extends Exception {
	public ResponseValidationException() {
	}

	public ResponseValidationException(String message) {
		super(message);
	}

	public ResponseValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
