package org.ngrinder.agent.service;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import net.grinder.communication.AgentControllerCommunicationDefaults;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.CompressionUtil.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Agent package service.
 *
 * @author Matt
 * @since 3.3
 */

@Service
public class AgentPackageService {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentPackageService.class);
	@Autowired
	private Config config;

	/**
	 * Get package name
	 *
	 * @param moduleName nGrinder module name.
	 * @return String module full name.
	 */
	public String getPackageName(String moduleName) {
		return moduleName + "-" + config.getVersion();
	}

	/**
	 * Get distributable package name with appropriate extension.
	 *
	 * @param moduleName   nGrinder sub  module name.
	 * @param connectionIP where it will connect to
	 * @param forWindow    if true, then package type is zip,if false, package type is tar.
	 * @return String  module full name.
	 */
	public String getDistributionPackageName(String moduleName, String connectionIP, boolean forWindow) {
		connectionIP = StringUtils.trimToEmpty(connectionIP);
		if (StringUtils.isNotEmpty(connectionIP)) {
			connectionIP = "-" + connectionIP;
		}
		return getPackageName(moduleName) + connectionIP + (forWindow ? ".zip" : ".tar");
	}

	/**
	 * Get the agent package containing folder.
	 */
	public File getAgentPackagesDir() {
		return config.isCluster() ? config.getExHome().getSubFile("download") : config.getHome().getSubFile("download");
	}

	/**
	 * Create agent package
	 *
	 * @param classLoader URLClass Loader.
	 * @return File
	 */
	public File createAgentPackage(URLClassLoader classLoader, String connectionIP) {
		File agentPackagesDir = getAgentPackagesDir();
		agentPackagesDir.mkdirs();
		File agentTar = new File(agentPackagesDir, getDistributionPackageName("ngrinder-core", connectionIP, false));
		if (agentTar.exists()) {
			return agentTar;
		}
		FileUtils.deleteQuietly(agentTar);
		final String basePath = "ngrinder-agent/";
		final String libPath = basePath + "lib/";
		TarArchiveOutputStream tarOutputStream = null;
		try {
			tarOutputStream = createTarArchiveStream(agentTar);
			addFolderToTar(tarOutputStream, basePath);
			addFolderToTar(tarOutputStream, libPath);
			Set<String> libs = getDependentLibs(classLoader);

			for (URL eachUrl : classLoader.getURLs()) {
				File eachClassPath = new File(eachUrl.getFile());
				if (!isJar(eachClassPath)) {
					continue;
				}
				if (isAgentDependentLib(eachClassPath, "ngrinder-sh")) {
					processJarEntries(eachClassPath, new TarArchivingZipEntryProcessor(tarOutputStream, basePath, 0100755));
				} else if (isAgentDependentLib(eachClassPath, libs)) {
					addFileToTar(tarOutputStream, eachClassPath, libPath + eachClassPath.getName());
				}
			}
			addAgentConfToTar(tarOutputStream, basePath, connectionIP);
		} catch (IOException e) {
			LOGGER.error("Error while generating an agent package" + e.getMessage());
		} finally {
			IOUtils.closeQuietly(tarOutputStream);
		}
		return agentTar;
	}

	private TarArchiveOutputStream createTarArchiveStream(File agentTar) throws IOException {
		FileOutputStream fos = new FileOutputStream(agentTar);
		return new TarArchiveOutputStream(new BufferedOutputStream(fos));
	}

	private void addAgentConfToTar(TarArchiveOutputStream tarOutputStream, String basePath,
	                               String connectionIP) throws IOException {
		if (StringUtils.isNotEmpty(connectionIP)) {
			final String config = getAgentConfigContent("agent_agent.conf", getAgentConfigParam(connectionIP));
			final byte[] bytes = config.getBytes();
			addInputStreamToTar(tarOutputStream, new ByteArrayInputStream(bytes), basePath + "__agent.conf",
					bytes.length, TarArchiveEntry.DEFAULT_FILE_MODE);
		}
	}

	private Set<String> getDependentLibs(URLClassLoader cl) throws IOException {
		Set<String> libs = new HashSet<String>();
		final String dependencies = IOUtils.toString(cl.getResourceAsStream("dependencies.txt"));
		for (String each : StringUtils.split(dependencies, ",;")) {
			libs.add(each.trim());
		}
		libs.add(getPackageName("ngrinder-core") + ".jar");
		return libs;
	}

	private Map<String, Object> getAgentConfigParam(String forServer) {
		Map<String, Object> confMap = newHashMap();
		confMap.put("controllerIP", forServer);
		final int port = config.getSystemProperties()
				.getPropertyInt(NGrinderConstants.NGRINDER_PROP_AGENT_CONTROL_PORT,
						AgentControllerCommunicationDefaults.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		confMap.put("controllerPort", String.valueOf(port));
		confMap.put("controllerRegion", config.getRegion());
		return confMap;
	}

	/**
	 * Check if this given path is jar.
	 *
	 * @param libFile lib file
	 * @return true if it's jar
	 */
	public boolean isJar(File libFile) {
		return StringUtils.endsWith(libFile.getName(), ".jar");
	}

	/**
	 * Check if this given lib file is the given library.
	 *
	 * @param libFile lib file
	 * @param libName desirable name
	 * @return true if dependent lib
	 */
	public boolean isAgentDependentLib(File libFile, String libName) {
		return StringUtils.startsWith(libFile.getName(), libName);
	}

	/**
	 * Check if this given lib file in the given lib set.
	 *
	 * @param libFile lib file
	 * @param libs    lib set
	 * @return true if dependent lib
	 */
	public boolean isAgentDependentLib(File libFile, Set<String> libs) {
		return libs.contains(libFile.getName());
	}

	/**
	 * Get the agent.config content replacing the variables with the given values.
	 *
	 * @param values map of configurations.
	 * @return generated string
	 */

	public String getAgentConfigContent(String templateName, Map<String, Object> values) {
		StringWriter writer = null;
		try {
			Configuration config = new Configuration();
			ClassPathResource cpr = new ClassPathResource("ngrinder_agent_home_template");
			config.setDirectoryForTemplateLoading(cpr.getFile());
			config.setObjectWrapper(new DefaultObjectWrapper());
			Template template = config.getTemplate(templateName);
			writer = new StringWriter();
			template.process(values, writer);
			return writer.toString();
		} catch (Exception e) {
			throw processException("Error while fetching the script template.", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	static class TarArchivingZipEntryProcessor implements ZipEntryProcessor {
		private TarArchiveOutputStream tao;
		private String basePath;
		private int mode;

		TarArchivingZipEntryProcessor(TarArchiveOutputStream tao, String basePath, int mode) {
			this.tao = tao;
			this.basePath = basePath;
			this.mode = mode;
		}

		@Override
		public void process(ZipFile file, ZipEntry entry) throws IOException {
			InputStream inputStream = null;
			try {
				inputStream = file.getInputStream(entry);
				addInputStreamToTar(this.tao, inputStream, basePath + FilenameUtils.getName(entry.getName()),
						entry.getSize(),
						this.mode);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

}
