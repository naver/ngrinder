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
package net.grinder.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Log compress utility. This class mainly provide the compress a single log file.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class LogCompressUtils {
	private static final int COMPRESS_BUFFER_SIZE = 8096;
	public static final Logger LOGGER = LoggerFactory.getLogger(LogCompressUtils.class);

	/**
	 * Compress multiple Files with the given encoding.
	 *
	 * @param logFiles     files to be compressed
	 * @param fromEncoding log file encoding
	 * @param toEncoding   compressed log file encoding
	 * @return compressed file byte array
	 */
	public static byte[] compress(File[] logFiles, Charset fromEncoding, Charset toEncoding) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		ByteArrayOutputStream out = null;
		ZipOutputStream zos = null;
		OutputStreamWriter osw = null;
		if (toEncoding == null) {
			toEncoding = Charset.defaultCharset();
		}
		if (fromEncoding == null) {
			fromEncoding = Charset.defaultCharset();
		}
		try {
			out = new ByteArrayOutputStream();
			zos = new ZipOutputStream(out);
			osw = new OutputStreamWriter(zos, toEncoding);
			for (File each : logFiles) {
				try {
					fis = new FileInputStream(each);
					isr = new InputStreamReader(fis, fromEncoding);
					ZipEntry zipEntry = new ZipEntry(each.getName());
					zipEntry.setTime(each.lastModified());
					zos.putNextEntry(zipEntry);
					char[] buffer = new char[COMPRESS_BUFFER_SIZE];
					int count;
					while ((count = isr.read(buffer, 0, COMPRESS_BUFFER_SIZE)) != -1) {
						osw.write(buffer, 0, count);
					}
					osw.flush();
					zos.flush();
					zos.closeEntry();
				} catch (IOException e) {
					LOGGER.error("Error occurs while compressing {} : {}", each.getAbsolutePath(), e.getMessage());
					LOGGER.debug("Details ", e);
				} finally {
					IOUtils.closeQuietly(isr);
					IOUtils.closeQuietly(fis);
				}
			}
			zos.finish();
			zos.flush();
			return out.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Error occurs while compressing log : {} ", e.getMessage());
			LOGGER.debug("Details : ", e);
			return null;
		} finally {
			IOUtils.closeQuietly(zos);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(osw);
		}
	}

	/**
	 * Compress the given file with the system encoding.
	 *
	 * @param logFiles files to be compressed
	 * @return compressed file byte array
	 */
	public static byte[] compress(File[] logFiles) {
		return compress(logFiles, Charset.defaultCharset(), Charset.defaultCharset());
	}

	/**
	 * Compress the given file with the system encoding.
	 *
	 * @param logFile file to be compressed
	 * @return compressed file byte array
	 */
	public static byte[] compress(File logFile) {
		return compress(new File[]{logFile}, Charset.defaultCharset(), Charset.defaultCharset());
	}

	/**
	 * Extracting the given byte array  into the given file.
	 *
	 * @param zipEntry byte array of compressed file
	 * @param toFile   file to be written
	 */
	public static void decompress(byte[] zipEntry, File toFile) {
		FileOutputStream fos = null;
		ByteArrayInputStream bio = new ByteArrayInputStream(zipEntry);
		try {
			fos = new FileOutputStream(toFile);
			decompress(bio, fos, Long.MAX_VALUE);
		} catch (IOException e) {
			LOGGER.error("Error occurs during extracting to {} : {} ", toFile.getAbsolutePath(), e.getMessage());
			LOGGER.debug("Details : ", e);
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(bio);
		}
	}

	/**
	 * Decompress the given the {@link InputStream} into the given {@link OutputStream}.
	 *
	 * @param inputStream  input stream of the compressed file
	 * @param outputStream file to be written
	 * @param limit        the limit of the output
	 */
	public static void decompress(InputStream inputStream, OutputStream outputStream, long limit) {
		ZipInputStream zipInputStream = null;
		try {
			zipInputStream = new ZipInputStream(inputStream);
			byte[] buffer = new byte[COMPRESS_BUFFER_SIZE];
			int count;
			long total = 0;
			checkNotNull(zipInputStream.getNextEntry(), "In zip, it should have at least one entry");
			do {
				while ((count = zipInputStream.read(buffer, 0, COMPRESS_BUFFER_SIZE)) != -1) {
					total += count;
					if (total >= limit) {
						break;
					}
					outputStream.write(buffer, 0, count);
				}
			} while (zipInputStream.getNextEntry() != null);
			outputStream.flush();
		} catch (IOException e) {
			LOGGER.error("Error occurs while decompressing {}", e.getMessage());
			LOGGER.debug("Details : ", e);
		} finally {
			IOUtils.closeQuietly(zipInputStream);
		}
	}

}
