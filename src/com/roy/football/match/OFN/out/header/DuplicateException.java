package com.roy.football.match.OFN.out.header;

public class DuplicateException extends RuntimeException {

	private static final long serialVersionUID = -8707465602178850548L;

	public DuplicateException() {
		super();
	}

	public DuplicateException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DuplicateException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateException(String message) {
		super(message);
	}

	public DuplicateException(Throwable cause) {
		super(cause);
	}

}
