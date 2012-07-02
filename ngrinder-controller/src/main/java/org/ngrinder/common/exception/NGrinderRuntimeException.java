package org.ngrinder.common.exception;

public class NGrinderRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 8662535812004958944L;

	public NGrinderRuntimeException(String message) {
		super(message);
	}

	public NGrinderRuntimeException(String message, Throwable e) {
		super(message, e);
	}
}
