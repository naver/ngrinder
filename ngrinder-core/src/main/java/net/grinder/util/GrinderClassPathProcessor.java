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
public abstract class GrinderClassPathProcessor {
	public GrinderClassPathProcessor() {
		init();
		initMore();
	}

	private static boolean junitContext = false;

	static void setJUnitContext() {
		junitContext = true;
	}

	/**
	 * Construct classPath for grinder from given classpath string.
	 * 
	 * @param classPath
	 *            classpath string
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public String filterClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isUselessJar(filename) || isUsefulReferenceProject(eachClassPath)) {
				logger.trace("classpath :" + eachClassPath);
				classPathList.add(eachClassPath);
			}

		}
		return StringUtils.join(classPathList, File.pathSeparator);
	}

	private boolean isUsefulReferenceProject(String path) {
		return junitContext && new File(path).isDirectory()
						&& path.contains(File.separator + "ngrinder-groovy" + File.separator);
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
	public String filterForeMostClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isForeMostJar(filename) || isUsefulForforeMostferenceProject(eachClassPath)) {
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
	public String filterPatchClassPath(String classPath, Logger logger) {
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

	private boolean isUsefulForforeMostferenceProject(String path) {
		return junitContext && new File(path).isDirectory()
						&& path.contains(File.separator + "ngrinder-dns" + File.separator);
	}

	private boolean isPatchJar(String jarFilename) {
		if ("jar".equals(FilenameUtils.getExtension(jarFilename))) {
			for (String jarName : PATCH_JAR_LIST) {
				if (jarFilename.contains(jarName)) {
					return true;
				}
			}
		}
		return false;
	}

	protected final List<String> FOREMOST_JAR_LIST = new ArrayList<String>();
	protected final List<String> PATCH_JAR_LIST = new ArrayList<String>();
	protected final List<String> USEFUL_JAR_LIST = new ArrayList<String>();
	protected final List<String> USELESS_JAR_LIST = new ArrayList<String>();

	public void init() {
		FOREMOST_JAR_LIST.add("ngrinder-dns");
		PATCH_JAR_LIST.add("patch.jar");
		// TODO: If we have need another jar files, we should append it here.
		USEFUL_JAR_LIST.add("grinder");
		USEFUL_JAR_LIST.add("dnsjava");
		USEFUL_JAR_LIST.add("asm");
		USEFUL_JAR_LIST.add("picocontainer");
		USEFUL_JAR_LIST.add("slf4j-api");
		USEFUL_JAR_LIST.add("json");
		USEFUL_JAR_LIST.add("logback");
		USEFUL_JAR_LIST.add("jna");
		USEFUL_JAR_LIST.add("jsr173");
		USEFUL_JAR_LIST.add("xmlbeans");
		USEFUL_JAR_LIST.add("stax-api");
		USEFUL_JAR_LIST.add("ngrinder-patch");
		USEFUL_JAR_LIST.add("junit");
		USEFUL_JAR_LIST.add("hamcrest");
		USEFUL_JAR_LIST.add("groovy");

		USELESS_JAR_LIST.add("jython-2.2");
		USELESS_JAR_LIST.add("ngrinder-core");
		USELESS_JAR_LIST.add("ngrinder-controller");
		USELESS_JAR_LIST.add("spring");
	}

	protected abstract void initMore();

	private boolean isForeMostJar(String jarFilename) {
		if ("jar".equals(FilenameUtils.getExtension(jarFilename))) {
			for (String jarName : FOREMOST_JAR_LIST) {
				if (jarFilename.contains(jarName)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isUselessJar(String jarFilename) {
		if (!"jar".equals(FilenameUtils.getExtension(jarFilename))) {
			return false;
		}

		for (String jarName : USEFUL_JAR_LIST) {
			if (jarFilename.contains(jarName)) {
				return true;
			}
		}

		for (String jarName : USELESS_JAR_LIST) {
			if (jarFilename.contains(jarName)) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Construct the foremost classPath from current classLoader.
	 * 
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public String buildForemostClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) GrinderClassPathProcessor.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return filterForeMostClassPath(builder.toString(), logger);
	}

	/**
	 * Construct classPath from current classLoader.
	 * 
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public String buildClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) GrinderClassPathProcessor.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return filterClassPath(builder.toString(), logger);
	}
}
