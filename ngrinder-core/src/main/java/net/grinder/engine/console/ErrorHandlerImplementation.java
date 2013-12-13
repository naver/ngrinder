/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.grinder.engine.console;

import net.grinder.console.common.ErrorHandler;

import org.slf4j.Logger;

/**
 * Custom {@link ErrorHandler} implementation.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public final class ErrorHandlerImplementation implements ErrorHandler {
	private final Logger m_logger;

	/**
	 * Constructor.
	 * 
	 * @param logger	logger
	 */
	public ErrorHandlerImplementation(Logger logger) {
		m_logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.common.ErrorHandler#handleErrorMessage(java.lang.String)
	 */
	@Override
	public void handleErrorMessage(String errorMessage) {
		m_logger.error(errorMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.common.ErrorHandler#handleErrorMessage(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void handleErrorMessage(String errorMessage, String title) {
		m_logger.error("[" + title + "] " + errorMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.common.ErrorHandler#handleException(java.lang.Throwable)
	 */
	@Override
	public void handleException(Throwable throwable) {
		m_logger.error(throwable.getMessage(), throwable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.common.ErrorHandler#handleException(java.lang.Throwable,
	 * java.lang.String)
	 */
	@Override
	public void handleException(Throwable throwable, String title) {
		m_logger.error(title, throwable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.common.ErrorHandler#handleInformationMessage(java.lang.String)
	 */
	@Override
	public void handleInformationMessage(String informationMessage) {
		m_logger.info(informationMessage);
	}
}