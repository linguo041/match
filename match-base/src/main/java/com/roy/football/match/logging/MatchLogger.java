package com.roy.football.match.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MatchLogger {
	private Logger logger = null;
	
	private MatchLogger (Logger logger) {
		this.logger =  logger;
	}
	
	public static MatchLogger getInstance(Class<?> clazz) {
		return new MatchLogger(Logger.getLogger(clazz));
	}

    public void error(ErrorType errorType, Object ...args) {
        logger.log(Level.ERROR, String.format("Error Code " + errorType.getCode() + " - " + errorType.getErrorMsg(), args));
    }
    
    public void error(ErrorType errorType, Throwable e, Object ...args) {
        logger.log(Level.ERROR, String.format("Error Code " + errorType.getCode() + " - " + errorType.getErrorMsg(), args), e);
    }

    public void error(String msg) {
        logger.log(Level.ERROR, msg);
    }

    public void error(String msg, Throwable e) {
        logger.log(Level.ERROR, msg, e);
    }

    public void warn(ErrorType errorType, Object ...args) {
        logger.log(Level.WARN, String.format(errorType.getCode() + " - " + errorType.getErrorMsg(), args));
    }
    
    public void warn(ErrorType errorType, Throwable e, Object ...args) {
        logger.log(Level.WARN, String.format(errorType.getCode() + " - " + errorType.getErrorMsg(), args), e);
    }
    
    public void warn(String msg, Throwable e) {
        logger.log(Level.WARN, msg, e);
    }

    public void warn(String msg) {
        logger.log(Level.WARN, msg);
    }

    public void debug(String msg, Throwable e) {
        logger.log(Level.DEBUG, msg, e);
    }

    public void debug(String msg) {
        logger.log(Level.DEBUG, msg);
    }

    public void log(String msg) {
        logger.log(Level.INFO, msg);
    }
}
