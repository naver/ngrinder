package net.grinder.engine.console;

import net.grinder.console.common.ErrorHandler;

import org.slf4j.Logger;

public final class ErrorHandlerImplementation implements ErrorHandler {
	private final Logger m_logger;

	public ErrorHandlerImplementation(Logger logger) {
		m_logger = logger;
	}

	public void handleErrorMessage(String errorMessage) {
		m_logger.error(errorMessage);
	}

	public void handleErrorMessage(String errorMessage, String title) {
		m_logger.error("[" + title + "] " + errorMessage);
	}

	public void handleException(Throwable throwable) {
		m_logger.error(throwable.getMessage(), throwable);
	}

	public void handleException(Throwable throwable, String title) {
		m_logger.error(title, throwable);
	}

	public void handleInformationMessage(String informationMessage) {
		m_logger.info(informationMessage);
	}
}