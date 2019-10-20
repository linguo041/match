package com.roy.football.match.httpRequest;

import com.roy.football.match.logging.ErrorType;

public class HttpRequestInterruptedException extends HttpRequestException {

	private static final long serialVersionUID = 6306089125541469897L;

	public HttpRequestInterruptedException(String requestUrl) {
		super(ErrorType.HttpRequestInterrupted, requestUrl);
	}
	
	public HttpRequestInterruptedException(Throwable cause, String requestUrl) {
		super(ErrorType.HttpRequestInterrupted, cause, requestUrl);
	}

}
