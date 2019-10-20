package com.roy.football.match.httpRequest;

import com.roy.football.match.logging.ErrorType;

public class HttpRequestException extends Exception{

	private static final long serialVersionUID = 7676901976363366272L;

	public HttpRequestException(ErrorType errorType, Object ...args) {
		super(String.format("Error Code " + errorType.getCode() + " - " + errorType.getErrorMsg(), args));
		this.setErrorType(errorType);
		this.setArgs(args);
	}
	
	public HttpRequestException(ErrorType errorType, Throwable cause, Object ...args) {
		super(String.format("Error Code " + errorType.getCode() + " - " + errorType.getErrorMsg(), args), cause);
		this.setErrorType(errorType);
		this.setArgs(args);
	}
	
	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public Object [] getArgs() {
		return args;
	}

	public void setArgs(Object [] args) {
		this.args = args;
	}
	
	private ErrorType errorType;
	private Object [] args;
}
