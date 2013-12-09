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
package net.grinder.lang;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;

import net.grinder.lang.groovy.GroovyHandler;
import net.grinder.lang.jython.JythonHandler;

import org.apache.commons.io.FilenameUtils;

/**
 * Supported Language Enum.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
public enum Lang {
	/**
	 * Groovy.
	 */
	Groovy(new GroovyHandler()),
	/**
	 * Jython.
	 */
	Jython(new JythonHandler()),
	/**
	 * Unknown.
	 */
	Unknown(new UnknownHandler());
	private final AbstractLanguageHandler handler;

	Lang(AbstractLanguageHandler handler) {
		this.handler = checkNotNull(handler);
	}

	/**
	 * Get {@link Lang} enum by the given file's name.
	 *
	 * @param file file
	 * @return {@link Lang} enum value
	 */
	public static Lang getByFileName(File file) {
		return getByFileName(file.getPath());
	}

	/**
	 * Get {@link Lang} enum by the given filename.
	 *
	 * @param fileName filename
	 * @return {@link Lang} enum value
	 */
	public static Lang getByFileName(String fileName) {
		String extension = FilenameUtils.getExtension(fileName);
		for (Lang each : values()) {
			if (each.getHandler().getExtension().equals(extension)) {
				return each;
			}
		}
		return Unknown;
	}

	/**
	 * Get the language handler.
	 *
	 * @return language handler.
	 */
	public AbstractLanguageHandler getHandler() {
		return handler;
	}

}
