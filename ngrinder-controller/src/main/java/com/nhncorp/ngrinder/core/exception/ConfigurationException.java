package com.nhncorp.ngrinder.core.exception;

@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException {
	public ConfigurationException(String message, Throwable t) {
		super(message, t);
	}

}
