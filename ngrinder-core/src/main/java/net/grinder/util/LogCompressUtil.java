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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log compress utility. This class mainly provide the compress a singl log file.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class LogCompressUtil {
	private static final int COMPRESS_BUFFER_SIZE = 8096;
	public static final Logger LOGGER = LoggerFactory.getLogger(LogCompressUtil.class);

	/**
	 * Compress the given file.
	 * 
	 * @param logFile
	 *            file to be compressed
	 * @return compressed file byte array
	 */
	public static byte[] compressFile(File logFile) {
		FileInputStream fio = null;
		ByteArrayOutputStream out = null;
		ZipOutputStream zos = null;
		try {
			fio = new FileInputStream(logFile);
			out = new ByteArrayOutputStream();
			zos = new ZipOutputStream(out);
			ZipEntry zipEntry = new ZipEntry("log.txt");
			zipEntry.setTime(new Date().getTime());
			zos.putNextEntry(zipEntry);
			byte[] buffer = new byte[COMPRESS_BUFFER_SIZE];
			int count = 0;
			while ((count = fio.read(buffer, 0, COMPRESS_BUFFER_SIZE)) != -1) {
				zos.write(buffer, 0, count);
			}
			zos.closeEntry();
			zos.finish();
			zos.flush();
			return out.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Error occurs while compress {}", logFile.getAbsolutePath());
			LOGGER.error("Details", e);
			return null;
		} finally {
			IOUtils.closeQuietly(zos);
			IOUtils.closeQuietly(fio);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Uncompress the given array into the given file.
	 * 
	 * @param zipEntry
	 *            byte array of compressed file
	 * @param toFile
	 *            file to be written
	 */
	public static void unCompress(byte[] zipEntry, File toFile) {
		FileOutputStream fos = null;
		ByteArrayInputStream bio = new ByteArrayInputStream(zipEntry);
		try {
			fos = new FileOutputStream(toFile);
			unCompress(bio, fos, Long.MAX_VALUE);
		} catch (IOException e) {
			LOGGER.error("Error occurs while uncompress {}", toFile.getAbsolutePath());
			LOGGER.error("Details", e);
			return;
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(bio);
		}
	}

	/**
	 * Uncompress the given the {@link InputStream} into the given {@link OutputStream}.
	 * 
	 * @param inputStream
	 *            input stream of the compressed file
	 * @param outputStream
	 *            file to be written
	 * @param limit
	 *            the limit of the output
	 */
	public static void unCompress(InputStream inputStream, OutputStream outputStream, long limit) {
		ZipInputStream zipInputStream = null;
		try {
			zipInputStream = new ZipInputStream(inputStream);
			byte[] buffer = new byte[COMPRESS_BUFFER_SIZE];
			int count = 0;
			long total = 0;
			checkNotNull(zipInputStream.getNextEntry(), "In zip, it should have at least one entry");
			while ((count = zipInputStream.read(buffer, 0, COMPRESS_BUFFER_SIZE)) != -1) {
				total += count;
				if (total >= limit) {
					break;
				}
				outputStream.write(buffer, 0, count);
			}
			outputStream.flush();
		} catch (IOException e) {
			LOGGER.error("Error occurs while uncompress");
			LOGGER.error("Details", e);
			return;
		} finally {
			IOUtils.closeQuietly(zipInputStream);
		}
	}

	/**
	 * Uncompress the given array into the given file.
	 * 
	 * @param zipEntry
	 *            byte array of compressed file
	 * @param toFile
	 *            file to be written
	 */
	public static void unCompressGzip(byte[] zipEntry, File toFile) {
		FileOutputStream fos = null;
		GZIPInputStream zipInputStream = null;
		ByteArrayInputStream bio = null;
		try {
			bio = new ByteArrayInputStream(zipEntry);
			zipInputStream = new GZIPInputStream(bio);
			fos = new FileOutputStream(toFile);
			byte[] buffer = new byte[COMPRESS_BUFFER_SIZE];
			int count = 0;
			while ((count = zipInputStream.read(buffer, 0, COMPRESS_BUFFER_SIZE)) != -1) {
				fos.write(buffer, 0, count);
			}
			fos.flush();
		} catch (IOException e) {
			LOGGER.error("Error occurs while uncompress {}", toFile.getAbsolutePath());
			LOGGER.error("Details", e);
			return;
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(bio);
			IOUtils.closeQuietly(zipInputStream);
		}
	}
}
