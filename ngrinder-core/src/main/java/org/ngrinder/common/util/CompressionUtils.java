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

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Compression utility.
 *
 * @author JunHo Yoon
 */
@SuppressWarnings("All")
public abstract class CompressionUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

	/**
	 * Unzip the given zipped file with given character set.
	 *
	 * @param zippedFile  zipped file
	 * @param charsetName character set
	 */
	public static void unzip(File zippedFile, String charsetName) {
		unzip(zippedFile, zippedFile.getParentFile(), charsetName);
	}

	/**
	 * Unzip the given zipped file into destination directory.
	 *
	 * @param zippedFile zipped file
	 * @param destDir    destination directory
	 */
	public static void unzip(File zippedFile, File destDir) {
		try {
			unzip(new FileInputStream(zippedFile), destDir, Charset.defaultCharset().name());
		} catch (FileNotFoundException e) {
			throw processException(e);
		}
	}

	/**
	 * Unzip the given zipped file into destination directory with the given
	 * character set.
	 *
	 * @param zippedFile  zipped file
	 * @param destDir     destination directory
	 * @param charsetName character set name
	 */
	public static void unzip(File zippedFile, File destDir, String charsetName) {
		try {
			unzip(new FileInputStream(zippedFile), destDir, charsetName);
		} catch (FileNotFoundException e) {
			throw processException(e);
		}
	}

	/**
	 * Unzip the given input stream into destination directory with the default
	 * character set.
	 *
	 * @param is      input stream
	 * @param destDir destination directory
	 */
	public static void unzip(InputStream is, File destDir) {
		unzip(is, destDir, Charset.defaultCharset().name());
	}

	/**
	 * Unzip the given input stream into destination directory with the given
	 * character set.
	 *
	 * @param is          input stream
	 * @param destDir     destination directory
	 * @param charsetName character set name
	 */
	public static void unzip(InputStream is, File destDir, String charsetName) {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		try {
			File folder = destDir;
			if (!folder.exists()) {
				folder.mkdir();
			}

			zis = new ZipInputStream(is);
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {
				String fileName = ze.getName();

				File newFile = new File(destDir.getAbsolutePath(), fileName);
				if (newFile.getPath().contains("..")) {
					throw new IllegalArgumentException("zip entry should not contain .. in the path.");
				}
				if (ze.isDirectory()) {
					newFile.mkdirs();
				} else {
					fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					IOUtils.closeQuietly(fos);
				}

				ze = zis.getNextEntry();
			}
		} catch (Exception e) {
			throw processException(e);
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(zis);
		}
	}

	/**
	 * Compresses the given file(or dir) and creates new file under the same
	 * directory.
	 *
	 * @param src file or directory
	 * @throws IOException IOException
	 */
	public static void zip(File src) throws IOException {
		zip(src, Charset.defaultCharset().name(), true);
	}

	/**
	 * Zips the given file(or dir) and create.
	 *
	 * @param src        file or directory to compress
	 * @param includeSrc if true and src is directory, then src is not included in the
	 *                   compression. if false, src is included.
	 * @throws IOException IOException
	 */
	public static void zip(File src, boolean includeSrc) throws IOException {
		zip(src, Charset.defaultCharset().name(), includeSrc);
	}

	/**
	 * Compresses the given src file (or directory) with the given encoding.
	 *
	 * @param src         src
	 * @param charSetName character set
	 * @param includeSrc  true if sub-directory will be zipped as well.
	 * @throws IOException IOException
	 */
	public static void zip(File src, String charSetName, boolean includeSrc) throws IOException {
		zip(src, src.getParentFile(), charSetName, includeSrc);
	}

	/**
	 * Compresses the given src file(or directory) and writes to the given
	 * output stream with sub directory.
	 *
	 * @param src src
	 * @param os  output stream
	 * @throws IOException IOException
	 */
	public static void zip(File src, OutputStream os) throws IOException {
		zip(src, os, Charset.defaultCharset().name(), true);
	}

	/**
	 * Compresses the given src file(or directory) and create the compressed
	 * file under the given destDir.
	 *
	 * @param src         src to be zipped.
	 * @param destDir     destination directory
	 * @param charSetName character set to be used
	 * @param includeSrc  true if sub-directory will be zipped as well.
	 * @throws IOException IOException
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
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(zippedFile);
			zip(src, os, charSetName, includeSrc);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	/**
	 * Zip the given src into the given output stream.
	 *
	 * @param src         src to be zipped
	 * @param os          output stream
	 * @param charsetName character set to be used
	 * @param includeSrc  true if src will be included.
	 * @throws IOException IOException
	 */
	public static void zip(File src, OutputStream os, String charsetName, boolean includeSrc) throws IOException {
		ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os);
		zos.setEncoding(charsetName);
		FileInputStream fis = null;

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
				File[] fs = checkNotNull(src.listFiles());
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
				File[] fs = checkNotNull(f.listFiles());
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
				try {
					fis = new FileInputStream(f);
					while ((length = fis.read(buf, 0, buf.length)) >= 0) {
						zos.write(buf, 0, length);
					}
				} finally {
					IOUtils.closeQuietly(fis);
				}
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
	 * The output file is created in the output folder, having the same name as
	 * the input file, minus the '.tar' extension.
	 *
	 * @param inFile    the input .tar file
	 * @param outputDir the output directory file.
	 * @return The {@link List} of {@link File}s with the untared content.
	 */
	@SuppressWarnings("resource")
	public static List<File> untar(final File inFile, final File outputDir) {
		final List<File> untaredFiles = new LinkedList<File>();
		InputStream is = null;
		TarArchiveInputStream debInputStream = null;
		try {
			is = new FileInputStream(inFile);
			debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
			TarArchiveEntry entry;
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
					try {
						IOUtils.copy(debInputStream, outputFileStream);
					} finally {
						IOUtils.closeQuietly(outputFileStream);
					}

					if (FilenameUtils.isExtension(outputFile.getName(), EXECUTABLE_EXTENSION)) {
						outputFile.setExecutable(true, true);
					}
					outputFile.setReadable(true);
					outputFile.setWritable(true, true);
				}
				untaredFiles.add(outputFile);
			}
		} catch (Exception e) {
			throw processException("Error while untar file", e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(debInputStream);
		}
		return untaredFiles;
	}

	/**
	 * Ungzip the given file.
	 *
	 * @param inFile  file
	 * @param outFile to
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
			int n;
			while (-1 != (n = gzIn.read(buffer))) {
				fout.write(buffer, 0, n);
			}
		} catch (Exception e) {
			throw processException("Error while ungzip file", e);
		} finally {
			IOUtils.closeQuietly(fin);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fout);
			IOUtils.closeQuietly(gzIn);
		}
		return outFile;
	}

	/**
	 * Process each jar entry in the  given jar file.
	 *
	 * @param jarFile   jar file
	 * @param processor jar file entry predicate
	 * @throws IOException thrown when having IO problem.
	 */
	public static void processJarEntries(File jarFile, ZipEntryProcessor processor) {
		try {
			JarFile jarfile = new JarFile(jarFile);
			Enumeration<java.util.jar.JarEntry> enu = jarfile.entries();
			while (enu.hasMoreElements()) {
				JarEntry je = enu.nextElement();
				if (je.isDirectory()) {
					continue;
				}
				processor.process(jarfile, je);
			}
		} catch (IOException e) {
			throw processException("Error while extracting jar file", e);
		}
	}


	/**
	 * Add a file into tar.
	 *
	 * @param tarStream TarArchive outputStream
	 * @param file      file
	 * @param path      relative path to append
	 * @throws IOException thrown when having IO problem.
	 */
	public static void addFileToTar(TarArchiveOutputStream tarStream, File file, String path) throws IOException {
		int mode = file.isDirectory() ? TarArchiveEntry.DEFAULT_DIR_MODE : TarArchiveEntry.DEFAULT_FILE_MODE;
		addFileToTar(tarStream, file, path, mode);
	}


	/**
	 * Add a folder into tar.
	 *
	 * @param tarStream TarArchive outputStream
	 * @param path      path to append
	 * @throws IOException thrown when having IO problem.
	 */
	public static void addFolderToTar(TarArchiveOutputStream tarStream, String path) throws IOException {
		TarArchiveEntry archiveEntry = new TarArchiveEntry(path);
		archiveEntry.setMode(TarArchiveEntry.DEFAULT_DIR_MODE);
		tarStream.putArchiveEntry(archiveEntry);
		tarStream.closeArchiveEntry();
	}


	/**
	 * Add the given input stream into tar.
	 *
	 * @param tarStream   TarArchive outputStream
	 * @param inputStream input stream
	 * @param path        relative path to append
	 * @param size        size of stream
	 * @param mode        mode for this entry
	 * @throws IOException thrown when having IO problem.
	 */
	public static void addInputStreamToTar(TarArchiveOutputStream tarStream, InputStream inputStream, String path,
	                                       long size, int mode) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(path);
		entry.setSize(size);
		entry.setMode(mode);
		try {
			tarStream.putArchiveEntry(entry);
			IOUtils.copy(inputStream, tarStream);
		} catch (IOException e) {
			throw processException("Error while adding File to Tar file", e);
		} finally {
			tarStream.closeArchiveEntry();
		}
	}

	/**
	 * Add the given byte into tar.
	 *
	 * @param tarStream   TarArchive outputStream
	 * @param data        data byte array
	 * @param path        relative path to append
	 * @param size        size of stream
	 * @param mode        mode for this entry
	 * @throws IOException thrown when having IO problem.
	 */
	public static void addByteToTar(TarArchiveOutputStream tarArchiveOutputStream, byte[] data, String path, long size, int mode) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(path);
		entry.setSize(size);
		entry.setMode(mode);
		try {
			tarArchiveOutputStream.putArchiveEntry(entry);
			tarArchiveOutputStream.write(data);
		} catch (IOException e) {
			throw processException("Error while adding File to Tar file", e);
		} finally {
			tarArchiveOutputStream.closeArchiveEntry();
		}
	}

	/**
	 * Add a file into tar.
	 *
	 * @param tarStream TarArchive outputStream
	 * @param file      file
	 * @param path      relative path to append
	 * @param mode      mode for this entry
	 * @throws IOException thrown when having IO problem.
	 */
	public static void addFileToTar(TarArchiveOutputStream tarStream, File file, String path, int mode) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(path);
		entry.setSize(file.length());
		entry.setMode(mode);
		BufferedInputStream bis = null;
		try {
			tarStream.putArchiveEntry(entry);
			bis = new BufferedInputStream(new FileInputStream(file));
			IOUtils.copy(bis, tarStream);
		} catch (IOException e) {
			throw processException("Error while adding File to Tar file", e);
		} finally {
			IOUtils.closeQuietly(bis);
			tarStream.closeArchiveEntry();
		}
	}


	public interface ZipEntryProcessor {
		public void process(ZipFile zipFile, ZipEntry je) throws IOException;
	}

	public interface FilePredicate {
		public boolean evaluate(Object object);
	}
}
