package ognjenj.charon.acct.exceptions;

public class MalformedPacketException extends Exception {
	public MalformedPacketException() {
	}

	public MalformedPacketException(String message) {
		super(message);
	}

	public MalformedPacketException(String message, Throwable cause) {
		super(message, cause);
	}
}
