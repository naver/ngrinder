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
package org.ngrinder.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File download utilities.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class FileDownloadUtil {

	private static final int FILE_DOWNLOAD_BUFFER_SIZE = 4096;
	private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadUtil.class);

	/**
	 * Provide file download from the given file path.
	 * @param response {@link HttpServletResponse}
	 * @param desFilePath file path
	 * @return true if succeeded
	 */
	public static boolean downloadFile(HttpServletResponse response, String desFilePath) {
		File desFile = new File(desFilePath);
		return downloadFile(response, desFile);
	}

	/**
	 * Provide file download from the given file path.
	 * @param response {@link HttpServletResponse}
	 * @param desFile file path
	 * @return true if succeeded
	 */
	public static boolean downloadFile(HttpServletResponse response, File desFile) {
		if (desFile == null || !desFile.exists()) {
			return false;
		}
		boolean result = true;
		response.reset();
		response.addHeader("Content-Disposition", "attachment;filename=" + desFile.getName());
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Length", "" + desFile.length());
		InputStream fis = null;
		byte[] buffer = new byte[FILE_DOWNLOAD_BUFFER_SIZE];
		OutputStream toClient = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(desFile));
			toClient = new BufferedOutputStream(response.getOutputStream());
			int readLength;
			while (((readLength = fis.read(buffer)) != -1)) {
				toClient.write(buffer, 0, readLength);
			}
			toClient.flush();
		} catch (FileNotFoundException e) {
			LOGGER.error("file not found:" + desFile.getAbsolutePath(), e);
			result = false;
		} catch (IOException e) {
			LOGGER.error("read file error:" + desFile.getAbsolutePath(), e);
			result = false;
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
		return result;
	}
}
