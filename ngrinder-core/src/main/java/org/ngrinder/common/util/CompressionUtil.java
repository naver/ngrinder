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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compression utility.
 * 
 * @author JunHo Yoon
 * 
 */
public abstract class CompressionUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtil.class);

	/**
	 * Upzip the given file on the same folder.
	 * 
	 * @param zippedFile
	 *            zipped file
	 * @throws IOException
	 *             IOException
	 */
	public static void unzip(File zippedFile) throws IOException {
		unzip(zippedFile, Charset.defaultCharset().name());
	}

	/**
	 * Unzip the given zipped file with given character set.
	 * 
	 * @param zippedFile
	 *            zipped file
	 * @param charsetName
	 *            character set
	 */
	public static void unzip(File zippedFile, String charsetName) {
		unzip(zippedFile, zippedFile.getParentFile(), charsetName);
	}

	/**
	 * Unzip the given zipped file into destination directory.
	 * 
	 * @param zippedFile
	 *            zipped file
	 * @param destDir
	 *            destination directory
	 */
	public static void unzip(File zippedFile, File destDir) {
		try {
			unzip(new FileInputStream(zippedFile), destDir, Charset.defaultCharset().name());
		} catch (FileNotFoundException e) {
			throw new NGrinderRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Unzip the given zipped file into destination directory with the given character set.
	 * 
	 * @param zippedFile
	 *            zipped file
	 * @param destDir
	 *            destination directory
	 * @param charsetName
	 *            character set name
	 */
	public static void unzip(File zippedFile, File destDir, String charsetName) {
		try {
			unzip(new FileInputStream(zippedFile), destDir, charsetName);
		} catch (FileNotFoundException e) {
			throw new NGrinderRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Unzip the given input stream into destination directory with the default character set.
	 * 
	 * @param is
	 *            input stream
	 * @param destDir
	 *            destination directory
	 */
	public static void unzip(InputStream is, File destDir) {
		unzip(is, destDir, Charset.defaultCharset().name());
	}

	/**
	 * Unzip the given input stream into destination directory with the given character set.
	 * 
	 * @param is
	 *            input stream
	 * @param destDir
	 *            destination directory
	 * @param charsetName
	 *            character set name
	 */
	public static void unzip(InputStream is, File destDir, String charsetName) {
		ZipArchiveInputStream zis = null;
		try {
			ZipArchiveEntry entry;
			String name;
			File target;
			int nWritten = 0;
			BufferedOutputStream bos;
			byte[] buf = new byte[1024 * 8];
			zis = new ZipArchiveInputStream(is, charsetName, false);
			while ((entry = zis.getNextZipEntry()) != null) {
				name = entry.getName();
				target = new File(destDir, name);
				if (entry.isDirectory()) {
					target.mkdirs(); /* does it always work? */
				} else {
					target.createNewFile();
					bos = new BufferedOutputStream(new FileOutputStream(target));
					while ((nWritten = zis.read(buf)) >= 0) {
						bos.write(buf, 0, nWritten);
					}
					bos.close();
				}
			}
		} catch (Exception e) {
			throw new NGrinderRuntimeException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(zis);
		}
	}

	/**
	 * Compresses the given file(or dir) and creates new file under the same directory.
	 * 
	 * @param src
	 *            file or directory
	 * @throws IOException
	 *             IOException
	 */
	public static void zip(File src) throws IOException {
		zip(src, Charset.defaultCharset().name(), true);
	}

	/**
	 * Zips the given file(or dir) and create.
	 * 
	 * @param src
	 *            file or directory to compress
	 * @param includeSrc
	 *            if true and src is directory, then src is not included in the compression. if
	 *            false, src is included.
	 * @throws IOException
	 *             IOException
	 */
	public static void zip(File src, boolean includeSrc) throws IOException {
		zip(src, Charset.defaultCharset().name(), includeSrc);
	}

	/**
	 * Compresses the given src file (or directory) with the given encoding.
	 * 
	 * @param src
	 *            src
	 * @param charSetName
	 *            character set
	 * @param includeSrc
	 *            true if sub-directory will be zipped as well.
	 * @throws IOException
	 *             IOException
	 */
	public static void zip(File src, String charSetName, boolean includeSrc) throws IOException {
		zip(src, src.getParentFile(), charSetName, includeSrc);
	}

	/**
	 * Compresses the given src file(or directory) and writes to the given output stream with sub
	 * directory.
	 * 
	 * @param src
	 *            src
	 * @param os
	 *            output stream
	 * 
	 * @throws IOException
	 *             IOException
	 */
	public static void zip(File src, OutputStream os) throws IOException {
		zip(src, os, Charset.defaultCharset().name(), true);
	}

	/**
	 * Compresses the given src file(or directory) and create the compressed file under the given
	 * destDir.
	 * 
	 * @param src
	 *            src to be zipped.
	 * @param destDir
	 *            destination directory
	 * @param charSetName
	 *            character set to be used
	 * @param includeSrc
	 *            true if sub-directory will be zipped as well.
	 * @throws IOException
	 *             IOException
	 */
	public static void zip(File src, File destDir, String charSetName, boolean includeSrc) throws IOException {
		String fileName = src.getName();
		if (!src.isDirectory()) {
			int pos = fileName.lastIndexOf(".");
			if (pos > 0) {
				fileName = fileName.substring(0, pos);
			}
		}
		fileName += ".zip";

		File zippedFile = new File(destDir, fileName);
		if (!zippedFile.exists()) {
			zippedFile.createNewFile();
		}
		zip(src, new FileOutputStream(zippedFile), charSetName, includeSrc);
	}

	/**
	 * Zip the given src into the given output stream.
	 * 
	 * @param src
	 *            src to be zipped
	 * @param os
	 *            output stream
	 * @param charsetName
	 *            character set to be used
	 * @param includeSrc
	 *            true if src will be included.
	 * @throws IOException
	 *             IOException
	 */
	public static void zip(File src, OutputStream os, String charsetName, boolean includeSrc) throws IOException {
		ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os);
		zos.setEncoding(charsetName);
		FileInputStream fis;

		int length;
		ZipArchiveEntry ze;
		byte[] buf = new byte[8 * 1024];
		String name;

		Stack<File> stack = new Stack<File>();
		File root;
		if (src.isDirectory()) {
			if (includeSrc) {
				stack.push(src);
				root = src.getParentFile();
			} else {
				File[] fs = src.listFiles();
				for (int i = 0; i < fs.length; i++) {
					stack.push(fs[i]);
				}
				root = src;
			}
		} else {
			stack.push(src);
			root = src.getParentFile();
		}

		while (!stack.isEmpty()) {
			File f = stack.pop();
			name = toPath(root, f);
			if (f.isDirectory()) {
				File[] fs = f.listFiles();
				for (int i = 0; i < fs.length; i++) {
					if (fs[i].isDirectory()) {
						stack.push(fs[i]);
					} else {
						stack.add(0, fs[i]);
					}
				}
			} else {
				ze = new ZipArchiveEntry(name);
				zos.putArchiveEntry(ze);
				fis = new FileInputStream(f);
				while ((length = fis.read(buf, 0, buf.length)) >= 0) {
					zos.write(buf, 0, length);
				}
				fis.close();
				zos.closeArchiveEntry();
			}
		}
		zos.close();
	}

	private static String toPath(File root, File dir) {
		String path = dir.getAbsolutePath();
		path = path.substring(root.getAbsolutePath().length()).replace(File.separatorChar, '/');
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (dir.isDirectory() && !path.endsWith("/")) {
			path += "/";
		}
		return path;
	}

	public static final List<String> EXECUTABLE_EXTENSION = Arrays.asList("bat", "sh");

	/**
	 * Untar an input file into an output file.
	 * 
	 * The output file is created in the output folder, having the same name as the input file,
	 * minus the '.tar' extension.
	 * 
	 * @param inFile
	 *            the input .tar file
	 * @param outputDir
	 *            the output directory file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 * @return The {@link List} of {@link File}s with the untared content.
	 * @throws ArchiveException
	 */
	public static List<File> untar(final File inFile, final File outputDir) {
		final List<File> untaredFiles = new LinkedList<File>();
		try {
			final InputStream is = new FileInputStream(inFile);
			final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
							.createArchiveInputStream("tar", is);
			TarArchiveEntry entry = null;
			while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
				final File outputFile = new File(outputDir, entry.getName());
				if (entry.isDirectory()) {
					if (!outputFile.exists()) {
						if (!outputFile.mkdirs()) {
							throw new IllegalStateException(String.format("Couldn't create directory %s.",
											outputFile.getAbsolutePath()));
						}
					}
				} else {
					File parentFile = outputFile.getParentFile();
					if (!parentFile.exists()) {
						parentFile.mkdirs();
					}
					final OutputStream outputFileStream = new FileOutputStream(outputFile);

					IOUtils.copy(debInputStream, outputFileStream);
					outputFileStream.close();
					if (FilenameUtils.isExtension(outputFile.getName(), EXECUTABLE_EXTENSION)) {
						outputFile.setExecutable(true, true);
					}
					outputFile.setReadable(true);
					outputFile.setWritable(true, true);
				}
				untaredFiles.add(outputFile);
			}
			debInputStream.close();
		} catch (Exception e) {
			LOGGER.error("Error while untar {} file by {}", inFile, e.getMessage());
			LOGGER.debug("Trace is : ", e);
			throw new NGrinderRuntimeException("Error while untar file", e);
		}
		return untaredFiles;
	}

	/**
	 * Ungzip the given file.
	 * 
	 * @param inFile
	 *            file
	 * @param outFile
	 *            to
	 * @return ungzipped file.
	 */
	public static File ungzip(final File inFile, final File outFile) {
		FileInputStream fin = null;
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		GzipCompressorInputStream gzIn = null;
		try {
			fin = new FileInputStream(inFile);
			in = new BufferedInputStream(fin);
			gzIn = new GzipCompressorInputStream(in);
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			fout = new FileOutputStream(outFile);
			final byte[] buffer = new byte[4048];
			int n = 0;
			while (-1 != (n = gzIn.read(buffer))) {
				fout.write(buffer, 0, n);
			}
		} catch (Exception e) {
			LOGGER.error("Error while ungzip {} file by {}", inFile, e.getMessage());
			LOGGER.debug("Trace is : ", e);
			throw new NGrinderRuntimeException("Error while ungzip file", e);
		} finally {
			IOUtils.closeQuietly(fin);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fout);
			IOUtils.closeQuietly(gzIn);
		}
		return outFile;
	}

}
