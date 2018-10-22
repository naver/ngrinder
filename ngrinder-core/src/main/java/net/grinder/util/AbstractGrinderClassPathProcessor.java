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

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
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
public abstract class AbstractGrinderClassPathProcessor {

	private final List<String> foreMostJarList = newArrayList();
	private final List<String> patchJarList = newArrayList();
	private final List<String> usefulJarList = newArrayList();
	private final List<String> uselessJarList = newArrayList();

	/**
	 * Constructor.
	 */
	public AbstractGrinderClassPathProcessor() {
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
	 * @param classPath classpath string
	 * @param logger    logger
	 * @return classpath optimized for grinder.
	 */
	public String filterClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isUsefulJar(filename) || isUsefulReferenceProject(eachClassPath)) {
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
	 * @param classPath classpath string
	 * @param logger    logger
	 * @return classpath optimized for grinder.
	 */
	public String filterForeMostClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isForemostJar(filename) || isUsefulForForemostReferenceProject(eachClassPath)) {
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
	 * @param classPath classpath string
	 * @param logger    logger
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

	private boolean isUsefulForForemostReferenceProject(String path) {
		return junitContext && new File(path).isDirectory()
				&& path.contains(File.separator + "ngrinder-runtime" + File.separator);
	}

	private boolean isPatchJar(String jarFilename) {
		if ("jar".equals(FilenameUtils.getExtension(jarFilename))) {
			for (String jarName : patchJarList) {
				if (jarFilename.contains(jarName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Initialize.
	 */
	public void init() {
		foreMostJarList.add("ngrinder-runtime");
		patchJarList.add("patch");
		// TODO: If we have need another jar files, we should append it here.
		usefulJarList.add("grinder");
		usefulJarList.add("dnsjava");
		usefulJarList.add("asm");
		usefulJarList.add("picocontainer");
		usefulJarList.add("slf4j-api");
		usefulJarList.add("json");
		usefulJarList.add("logback");
		usefulJarList.add("jna");
		usefulJarList.add("jsr173");
		usefulJarList.add("xmlbeans");
		usefulJarList.add("stax-api");
		usefulJarList.add("ngrinder-patch");
		usefulJarList.add("junit");
		usefulJarList.add("hamcrest");
		usefulJarList.add("commons-lang");

		uselessJarList.add("jython-2.2");
		uselessJarList.add("ngrinder-core");
		uselessJarList.add("ngrinder-controller");
		uselessJarList.add("ngrinder-groovy");
		uselessJarList.add("spring");
	}

	protected abstract void initMore();

	private boolean isForemostJar(String jarFilename) {
		if ("jar".equals(FilenameUtils.getExtension(jarFilename))) {
			for (String jarName : foreMostJarList) {
				if (jarFilename.contains(jarName)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isUsefulJar(String jarFilename) {
		if (isPatchJar(jarFilename)) {
			return false;
		}

		if (!"jar".equals(FilenameUtils.getExtension(jarFilename))) {
			return false;
		}

		for (String jarName : uselessJarList) {
			if (jarFilename.contains(jarName)) {
				return false;
			}
		}

		for (String jarName : usefulJarList) {
			if (jarFilename.contains(jarName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Construct the foremost classPath from current classLoader.
	 *
	 * @param logger logger
	 * @return classpath optimized for grinder.
	 */
	public String buildForemostClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) AbstractGrinderClassPathProcessor.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return filterForeMostClassPath(builder.toString(), logger);
	}

	/**
	 * Construct the patch classPath from current classLoader.
	 *
	 * @param logger logger
	 * @return classpath optimized for grinder.
	 */
	public String buildPatchClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) AbstractGrinderClassPathProcessor.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return filterPatchClassPath(builder.toString(), logger);
	}

	/**
	 * Construct classPath from current classLoader.
	 *
	 * @param logger logger
	 * @return classpath optimized for grinder.
	 */
	public String buildClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urls = ((URLClassLoader) AbstractGrinderClassPathProcessor.class.getClassLoader()).getURLs();

		StringBuilder builder = new StringBuilder();
		for (URL each : urls) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		if (builder.length() < 300) {
			// In case of the URLClassLoader is not activated, Try with system class path
			final String property = System.getProperty("java.class.path", "");
			for (String each : property.split(File.pathSeparator)) {
				builder.append(each).append(File.pathSeparator);
			}
		}
		return filterClassPath(builder.toString(), logger);
	}

	public List<String> getUsefulJarList() {
		return usefulJarList;
	}

	public List<String> getUselessJarList() {
		return uselessJarList;
	}

}
