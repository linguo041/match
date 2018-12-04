package com.roy.football.match.OFN.parser;

import com.roy.football.match.Exception.CommonException;
import com.roy.football.match.logging.ErrorType;

public class MatchParseException extends CommonException {

	private static final long serialVersionUID = 6444556112284733198L;

	public MatchParseException(String args, Throwable cause){
		super(ErrorType.UnableToPerseMatchData, cause, args);
	}
}
