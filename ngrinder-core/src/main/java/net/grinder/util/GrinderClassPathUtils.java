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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * Grinder classpath optimization class.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class GrinderClassPathUtils {
	/**
	 * Construct classPath for grinder from given classpath string.
	 * 
	 * @param classPath
	 *            classpath string
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String filterClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isNotJarOrUselessJar(filename)) {
				continue;
			}

			logger.trace("classpath :" + eachClassPath);
			classPathList.add(eachClassPath);
		}
		return StringUtils.join(classPathList, File.pathSeparator);
	}

	/**
	 * Construct the classpath of ngrinder which is very important and located in the head of
	 * classpath.
	 * 
	 * @param classPath
	 *            classpath string
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String filterForeMostClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isForeMostJar(filename)) {
				logger.trace("classpath :" + eachClassPath);
				classPathList.add(eachClassPath);
			}
		}
		return StringUtils.join(classPathList, File.pathSeparator);
	}
	
	/**
	 * Construct the classpath of ngrinder which is very important and located in the head of
	 * classpath.
	 * 
	 * @param classPath
	 *            classpath string
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String filterPatchClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isPatchJar(filename)) {
				logger.trace("classpath :" + eachClassPath);
				classPathList.add(eachClassPath);
			}
		}
		return StringUtils.join(classPathList, File.pathSeparator);
	}

	private static boolean isPatchJar(String jarFilename) {
		if ("jar".equals(FilenameUtils.getExtension(jarFilename))) {
			for (String jarName : PATCH_JAR_LIST) {
				if (jarFilename.contains(jarName)) {
					return true;
				}
			}
		}
		return false;
	}

	private static final List<String> FOREMOST_JAR_LIST = new ArrayList<String>();
	private static final List<String> PATCH_JAR_LIST = new ArrayList<String>();
	private static final List<String> USEFUL_JAR_LIST = new ArrayList<String>();
	private static final List<String> USELESS_JAR_LIST = new ArrayList<String>();
	static {
		FOREMOST_JAR_LIST.add("ngrinder-dns");
		PATCH_JAR_LIST.add("patch.jar");
		// TODO: If we have need another jar files, we should append it here.
		USEFUL_JAR_LIST.add("grinder");
		USEFUL_JAR_LIST.add("dnsjava");
		USEFUL_JAR_LIST.add("asm");
		USEFUL_JAR_LIST.add("picocontainer");
		USEFUL_JAR_LIST.add("jython-2.5");
		USEFUL_JAR_LIST.add("jython-standalone-2.5");
		USEFUL_JAR_LIST.add("slf4j-api");
		USEFUL_JAR_LIST.add("json");
		USEFUL_JAR_LIST.add("logback");
		USEFUL_JAR_LIST.add("jna");
		USEFUL_JAR_LIST.add("jsr173");
		USEFUL_JAR_LIST.add("xmlbeans");
		USEFUL_JAR_LIST.add("stax-api");
		USEFUL_JAR_LIST.add("ngrinder-patch");
		
		USELESS_JAR_LIST.add("jython-2.2");
		USELESS_JAR_LIST.add("ngrinder-core");
		USELESS_JAR_LIST.add("ngrinder-controller");
		USELESS_JAR_LIST.add("spring");

	}

	private static boolean isForeMostJar(String jarFilename) {
		if ("jar".equals(FilenameUtils.getExtension(jarFilename))) {
			for (String jarName : FOREMOST_JAR_LIST) {
				if (jarFilename.contains(jarName)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isNotJarOrUselessJar(String jarFilename) {
		if (!"jar".equals(FilenameUtils.getExtension(jarFilename))) {
			return true;
		}
		for (String jarName : USELESS_JAR_LIST) {
			if (jarFilename.contains(jarName)) {
				return true;
			}
		}
		for (String jarName : USEFUL_JAR_LIST) {
			if (jarFilename.contains(jarName)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Construct the foremost classPath from current classLoader.
	 * 
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String buildForemostClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) GrinderClassPathUtils.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return GrinderClassPathUtils.filterForeMostClassPath(builder.toString(), logger);
	}

	/**
	 * Construct classPath from current classLoader.
	 * 
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String buildClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) GrinderClassPathUtils.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return GrinderClassPathUtils.filterClassPath(builder.toString(), logger);
	}
}
