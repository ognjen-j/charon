package ognjenj.charon.acct.exceptions;

public class OvpnClientStatusFileNotFoundException extends Exception {
	public OvpnClientStatusFileNotFoundException() {
	}

	public OvpnClientStatusFileNotFoundException(String message) {
		super(message);
	}

	public OvpnClientStatusFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
