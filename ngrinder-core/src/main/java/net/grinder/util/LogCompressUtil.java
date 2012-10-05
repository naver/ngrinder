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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
		ZipInputStream zipInputStream = null;
		ByteArrayInputStream bio = null;
		try {
			bio = new ByteArrayInputStream(zipEntry);
			zipInputStream = new ZipInputStream(bio);
			fos = new FileOutputStream(toFile);
			byte[] buffer = new byte[COMPRESS_BUFFER_SIZE];
			int count = 0;
			checkNotNull(zipInputStream.getNextEntry(), "In zip, it should have at least one entry");
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
