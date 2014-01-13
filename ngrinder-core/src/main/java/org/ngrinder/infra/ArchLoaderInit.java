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
import java.net.URLDecoder;
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

		try {
			JarFile jarfile = new JarFile(getSigarNativePath());
			for (Enumeration<JarEntry> en = jarfile.entries(); en.hasMoreElements(); ) {
				JarEntry je = en.nextElement();
				if (name.contains(je.getName())) {
					FileOutputStream fo = null;
					InputStream is = null;
					try {
						is = jarfile.getInputStream(je);
						fo = new FileOutputStream(fl);
						IOUtils.copy(is, fo);
					} finally {
						IOUtils.closeQuietly(fo);
						IOUtils.closeQuietly(is);
					}
				}
			}
		} catch (IOException e) {
			throw new ArchLoaderException(e.getMessage());
		}
	}


	private void addNativeDirectoryToLibPath(File nativeDirectory) {
		String existingPath = System.getProperty("java.library.path");
		if (!existingPath.contains(nativeDirectory.getAbsolutePath())) {
			System.setProperty("java.library.path", nativeDirectory.getAbsolutePath() + File
					.pathSeparator + existingPath);
		}
	}

	private String getSigarNativePath() throws IOException {
		// Current class loader first
		final ClassLoader classLoader = ArchLoaderInit.class.getClassLoader();
		for (URL each : ((URLClassLoader) classLoader).getURLs()) {
			if (each.getFile().contains("sigar-native-")) {
				return URLDecoder.decode(each.getFile(), "UTF-8");
			}
		}

		// Then parent class loader
		final ClassLoader parent = classLoader.getParent();
		if (parent != null) {
			for (URL each : ((URLClassLoader) parent).getURLs()) {
				if (each.getFile().contains("sigar-native-")) {
					return URLDecoder.decode(each.getFile(), "UTF-8");
				}
			}
		}

		// Try with system class path
		final String property = System.getProperty("java.class.path", "");
		for (String each : property.split(File.pathSeparator)) {
			if (each.contains("sigar-native-")) {
				return new File(each).getAbsolutePath();
			}
		}
		throw new IOException("No sigar-native available in the classpath");
	}
}
