package com.roy.football.match.util;

import com.roy.football.match.Exception.CommonException;
import com.roy.football.match.logging.ErrorType;

public class XmlParseException extends CommonException {

	private static final long serialVersionUID = 4945842380090460793L;

	public XmlParseException(ErrorType errorType, Object ...args) {
		super(errorType, args);
	}
	
	public XmlParseException(ErrorType errorType, Throwable cause, Object ...args) {
		super(errorType, cause, args);
	}

}
