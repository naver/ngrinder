package org.ngrinder.infra;

import org.apache.commons.io.IOUtils;
import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.SigarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ArchLoader initializer
 *
 * @since 3.3
 */
public class ArchLoaderInit {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArchLoaderInit.class);

	public void init(File nativeDirectory) throws
			ArchLoaderException, ArchNotSupportedException {
		final SigarLoader archLoader = new SigarLoader(getClass());
		archLoader.setName("sigar");
		final String name = archLoader.getLibraryName();
		File fl = new File(nativeDirectory, name);
		addNativeDirectoryToLibPath(nativeDirectory);
		if (fl.exists()) {
			return;
		}

		FileOutputStream fo = null;
		InputStream is = null;
		try {
			is = getClass().getClassLoader().getResourceAsStream(name);
			JarFile jarfile = new JarFile(getSigarNativePath());
			for (Enumeration<JarEntry> en = jarfile.entries(); en.hasMoreElements(); ) {
				JarEntry je = en.nextElement();
				if (name.contains(je.getName())) {
					is = jarfile.getInputStream(je);
					fo = new FileOutputStream(fl);
					IOUtils.copy(is, fo);
				}
			}
		} catch (IOException e) {
			throw new ArchLoaderException(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fo);
			IOUtils.closeQuietly(is);
		}
	}

	private void addNativeDirectoryToLibPath(File nativeDirectory) {
		String existingPath = System.getProperty("java.library.path");
		if (!existingPath.contains(nativeDirectory.getAbsolutePath())) {
			System.setProperty("java.library.path", nativeDirectory.getAbsolutePath() + File
					.pathSeparator + existingPath);
		}
		LOGGER.info("java.library.path : {} ", System.getProperty("java.library.path"));
	}

	private String getSigarNativePath() throws IOException {
		for (URL each : ((URLClassLoader) ArchLoaderInit.class.getClassLoader()).getURLs()) {
			if (each.getFile().contains("sigar-native-")) {
				return each.getFile();
			}
		}
		throw new IOException("No sigar-native available in the classpath");
	}
}
