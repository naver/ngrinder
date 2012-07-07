/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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