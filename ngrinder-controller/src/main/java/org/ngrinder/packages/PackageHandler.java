package org.ngrinder.packages;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.*;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.CompressionUtils.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;

public abstract class PackageHandler {

	protected final int TIME_MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
	private final int EXEC = 0x81ed;

	@Autowired
	private Config config;

	private Logger LOGGER = LoggerFactory.getLogger(PackageHandler.class);

	protected Set<String> getDependentLibs(URLClassLoader urlClassLoader) {
		Set<String> libs = new HashSet<>();
		try (InputStream dependencyStream = urlClassLoader.getResourceAsStream(this.getDependenciesFileName())) {
			final String dependencies = IOUtils.toString(dependencyStream);
			for (String each : StringUtils.split(dependencies, ";")) {
				libs.add(each.trim().replace("-SNAPSHOT", ""));
			}
		} catch (Exception e) {
			LOGGER.error("Error while loading " + this.getDependenciesFileName(), e);
		}
		return libs;
	}

	protected void cleanUpPackageDir(boolean force) {
		synchronized (AgentPackageService.class) {
			final File packagesDir = getPackageDir();
			final File[] files = packagesDir.listFiles();
			if (files != null) {
				for (File each : files) {
					if (!each.isDirectory()) {
						long expiryTimestamp = each.lastModified() + (TIME_MILLIS_OF_DAY * 2);
						if (force || expiryTimestamp < System.currentTimeMillis()) {

							FileUtils.deleteQuietly(each);
						}
					}
				}
			}
		}
	}

	public void addBaseFolderToPackage(TarArchiveOutputStream tarArchiveOutputStream) throws IOException {
		addFolderToTar(tarArchiveOutputStream, getBasePath());
		addFolderToTar(tarArchiveOutputStream, getLibPath());
	}

	public void addFileToPackage(TarArchiveOutputStream tarArchiveOutputStream, File file, String path) throws IOException {
		addFileToTar(tarArchiveOutputStream, file, getLibPath() + path);
	}

	public void addConfigToPackage(TarArchiveOutputStream tarOutputStream, Map<String, Object> agentConfigParam) throws IOException {
		final String config = convertToConfigString(agentConfigParam);
		final byte[] bytes = config.getBytes();
		addInputStreamToTar(tarOutputStream, new ByteArrayInputStream(bytes), getBasePath() + "__agent.conf",
			bytes.length, TarArchiveEntry.DEFAULT_FILE_MODE);
	}

	public File getPackageFile(String regionName, String connectionIP, String ownerName, boolean forWindow) {
		File packageDir = getPackageDir();
		if (packageDir.mkdirs()) {
			LOGGER.info("{} is created", packageDir.getPath());
		}
		final String packageName = getDistributionPackageName(regionName, connectionIP, ownerName, forWindow);
		return new File(packageDir, packageName);
	}

	public void copyShellFile(TarArchiveOutputStream tarOutputStream) throws IOException {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(getShellScriptsPath());
		byte[] shellFileBytes;
		for (Resource resource : resources) {
			try (InputStream shellFileIs = resource.getInputStream()) {
				shellFileBytes = IOUtils.toByteArray(shellFileIs);
				addByteToTar(tarOutputStream, shellFileBytes, getBasePath() + FilenameUtils.getName(resource.getFilename()),
					shellFileBytes.length, EXEC);
			}
		}
	}

	/**
	 * Get the agent package containing folder.
	 *
	 * @return File  agent package dir.
	 */
	private File getPackageDir() {
		return config.getHome().getSubFile("download");
	}

	/**
	 * Get distributable package name with appropriate extension.
	 *
	 * @param regionName   region   namee
	 * @param connectionIP where it will connect to
	 * @param ownerName    owner name
	 * @param forWindow    if true, then package type is zip,if false, package type is tar.
	 * @return String  module full name.
	 */
	private String getDistributionPackageName(String regionName, String connectionIP, String ownerName, boolean forWindow) {
		return getPackageName() + getFilenamePostFix(regionName) + getFilenamePostFix(connectionIP) +
			getFilenamePostFix(ownerName) + (forWindow ? ".zip" : ".tar");
	}

	private String getFilenamePostFix(String value) {
		value = trimToEmpty(value);
		if (isNotEmpty(value)) {
			value = "-" + value;
		}
		return value;
	}

	/**
	 * Get the agent.config content replacing the variables with the given values.
	 *
	 * @param values       map of configurations.
	 * @return generated string
	 */
	private String convertToConfigString(Map<String, Object> values) {
		try (StringWriter writer = new StringWriter()) {
			Configuration config = new Configuration();
			config.setClassForTemplateLoading(this.getClass(), "/ngrinder_agent_home_template");
			config.setObjectWrapper(new DefaultObjectWrapper());
			Template template = config.getTemplate(getTemplateName());
			template.process(values, writer);
			return writer.toString();
		} catch (Exception e) {
			throw processException("Error while fetching the script template.", e);
		}
	}

	/**
	 * Get package name
	 *
	 * @return String module full name.
	 */
	private String getPackageName() {
		return this.getModuleName() + "-" + config.getVersion();
	}

	public abstract Map<String, Object> getConfigParam(String regionName, String controllerIP, int port, String owner);

	public abstract Set<String> getPackageDependentLibs(URLClassLoader urlClassLoader);

	protected  abstract String getModuleName();

	protected abstract String getBasePath();

	protected abstract String getLibPath();

	protected abstract String getShellScriptsPath();

	protected abstract String getTemplateName();

	protected abstract String getDependenciesFileName();
}
