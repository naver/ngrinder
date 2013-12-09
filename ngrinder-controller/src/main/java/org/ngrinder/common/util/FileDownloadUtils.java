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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * File download utilities.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class FileDownloadUtils {

	private static final int FILE_DOWNLOAD_BUFFER_SIZE = 4096;
	private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadUtils.class);
	public static final int FILE_CHUNK_BUFFER_SIZE = 1024 * 1024;

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
		InputStream fis = null;
		byte[] buffer = new byte[FILE_DOWNLOAD_BUFFER_SIZE];
		OutputStream toClient = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			toClient = new BufferedOutputStream(response.getOutputStream());
			int readLength;
			while (((readLength = fis.read(buffer)) != -1)) {
				toClient.write(buffer, 0, readLength);
			}
			toClient.flush();
		} catch (FileNotFoundException e) {
			LOGGER.error("file not found:" + file.getAbsolutePath(), e);
			result = false;
		} catch (IOException e) {
			LOGGER.error("read file error:" + file.getAbsolutePath(), e);
			result = false;
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
		return result;
	}
}
