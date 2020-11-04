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

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.apache.commons.io.IOUtils.copyLarge;

/**
 * File download utilities.
 *
 * @since 3.0
 */
@Slf4j
public abstract class FileDownloadUtils {

	/**
	 * Download the given file to the given {@link HttpServletResponse}.
	 *
	 * @param response {@link HttpServletResponse}
	 * @param fileName file path
	 * @return true if succeeded
	 */
	public static boolean downloadFile(HttpServletResponse response, String fileName) {
		File file = new File(fileName);
		return downloadFile(response, file);
	}

	/**
	 * Download the given file to the given {@link HttpServletResponse}.
	 *
	 * @param response {@link HttpServletResponse}
	 * @param file     file path
	 * @return true if succeeded
	 */
	public static boolean downloadFile(HttpServletResponse response, File file) {
		if (file == null || !file.exists()) {
			return false;
		}

		boolean result = true;
		response.reset();
		response.addHeader("Content-Disposition", "attachment;filename=" + file.getName());
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Length", "" + file.length());

		try (InputStream fis = new BufferedInputStream(new FileInputStream(file));
			 OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {
			copyLarge(fis, toClient);
		} catch (FileNotFoundException e) {
			log.error("file not found:" + file.getAbsolutePath(), e);
			result = false;
		} catch (IOException e) {
			log.error("read file error:" + file.getAbsolutePath(), e);
			result = false;
		}

		return result;
	}
}
