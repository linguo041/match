package com.roy.football.match.Exception;

import com.roy.football.match.logging.ErrorType;

public class CommonException extends Exception{
private static final long serialVersionUID = 1196116378666755633L;
	
	public CommonException(){
		super();
	}
	
	public CommonException(String message){
		super(message);
	}
	
	public CommonException(String message, Throwable cause){
		super(message, cause);
	}
	
	public CommonException (Throwable cause){
		super(cause);
	}
	
	protected CommonException(String message, Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CommonException (ErrorType errorType, Object ...args) {
		super(String.format("Error Code " + errorType.getCode() + " - " + errorType.getErrorMsg(), args));
		this.setErrorType(errorType);
		this.setArgs(args);
	}
	
	public CommonException (ErrorType errorType, Throwable cause, Object ...args) {
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
