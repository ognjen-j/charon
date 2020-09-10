package ognjenj.charon.web.exceptions;

public class IncompleteConfigurationException extends Exception {
	public IncompleteConfigurationException() {
	}

	public IncompleteConfigurationException(String message) {
		super(message);
	}

	public IncompleteConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
