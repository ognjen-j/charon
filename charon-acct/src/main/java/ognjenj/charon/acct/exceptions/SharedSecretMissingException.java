package ognjenj.charon.acct.exceptions;

public class SharedSecretMissingException extends Exception {
	public SharedSecretMissingException() {
	}

	public SharedSecretMissingException(String message) {
		super(message);
	}

	public SharedSecretMissingException(String message, Throwable cause) {
		super(message, cause);
	}
}
