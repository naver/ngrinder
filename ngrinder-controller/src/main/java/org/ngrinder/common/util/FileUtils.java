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
package org.ngrinder.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.apache.commons.io.FileUtils.*;

/**
 * Convenient File utilities.
 *
 * @since 3.1
 */
@Slf4j
public abstract class FileUtils {

	/**
	 * Copy the given resource to the given file.
	 *
	 * @param resourcePath resource path
	 * @param file         file to write
	 * @since 3.2
	 */
	public static void copyResourceToFile(String resourcePath, File file) {
		copyResourceToFile(new ClassPathResource(resourcePath), file);
	}

	public static void copyResourceToFile(Resource resource, File file) {
		try (InputStream io = resource.getInputStream()) {
			copyToFile(io, file);
		} catch (IOException e) {
			log.error("error while writing {}", resource.getFilename(), e);
		}
	}

}
