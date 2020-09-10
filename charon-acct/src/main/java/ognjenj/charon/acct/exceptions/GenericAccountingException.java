package ognjenj.charon.acct.exceptions;

public class GenericAccountingException extends Exception {
	public GenericAccountingException(Throwable cause) {
		super(cause);
	}

	public GenericAccountingException() {
	}

	public GenericAccountingException(String message) {
		super(message);
	}

	public GenericAccountingException(String message, Throwable cause) {
		super(message, cause);
	}
}
