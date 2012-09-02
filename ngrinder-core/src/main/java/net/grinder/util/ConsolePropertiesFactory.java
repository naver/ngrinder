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
package net.grinder.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;

import net.grinder.SingleConsole;
import net.grinder.console.model.ConsoleProperties;

/**
 * Convenient class for {@link ConsoleProperties} creation.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class ConsolePropertiesFactory {
	/**
	 * Create empty {@link ConsoleProperties}. the created
	 * {@link ConsoleProperties} instance links with temp/temp_console
	 * directory.
	 * 
	 * @return empty {@link ConsoleProperties} instance
	 * 
	 */
	public static ConsoleProperties createEmptyConsoleProperties() {
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("ngrinder", "tmp");
			ConsoleProperties consoleProperties = new ConsoleProperties(SingleConsole.RESOURCE, tmpFile);

			return consoleProperties;
		} catch (Exception e) {
			String message = "Exception occurs while merging entities while creating empty console";
			throw new NGrinderRuntimeException(message, e);
		} finally {
			FileUtils.deleteQuietly(tmpFile);
		}
	}
}
