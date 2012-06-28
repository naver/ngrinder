package com.nhncorp.ngrinder.core.util;

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

public class FileUtil {

	private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

	public static boolean downloadFile(HttpServletResponse response, String desFilePath) {
		File desFile = new File(desFilePath);
		return downloadFile(response, desFile);
	}

	public static boolean downloadFile(HttpServletResponse response, File desFile) {
		boolean result = true;
		response.reset();
		response.addHeader("Content-Disposition", "attachment;filename=" + desFile.getName());
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Length", "" + desFile.length());
		InputStream fis = null;
		byte[] buffer = new byte[4096];
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
			LOG.error("file not found:" + desFile.getAbsolutePath(), e);
			result = false;
		} catch (IOException e) {
			LOG.error("read file error:" + desFile.getAbsolutePath(), e);
			result = false;
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
		return result;
	}
}
