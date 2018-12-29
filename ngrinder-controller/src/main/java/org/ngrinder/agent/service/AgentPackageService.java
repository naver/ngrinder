package org.ngrinder.agent.service;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.packages.AgentPackageHandler;
import org.ngrinder.packages.MonitorPackageHandler;
import org.ngrinder.packages.PackageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.util.CompressionUtils.*;

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

	@Autowired
	@Qualifier("agentPackageHandler")
	private AgentPackageHandler agentPackageHandler;

	/**
	 * Create package from PackageHandler.
	 *
	 * @return File package.
	 */
	public File createPackage(PackageHandler packageHandler, URLClassLoader classLoader, String regionName, String connectionIP, int port, String owner) {
		synchronized (AgentPackageService.class) {
			File packageFile = packageHandler.getPackageFile(regionName, connectionIP, owner, false);
			if (packageFile.exists()) {
				return packageFile;
			}
			FileUtils.deleteQuietly(packageFile);
			try (TarArchiveOutputStream tarOutputStream = createTarArchiveStream(packageFile)) {
				addDependentLibToTarStream(packageHandler, tarOutputStream, classLoader);
				if (!(packageHandler instanceof AgentPackageHandler) || isNotEmpty(connectionIP)) {
					packageHandler.addConfigToPackage(tarOutputStream, packageHandler.getConfigParam(regionName, connectionIP, port, owner));
				}
			} catch (Exception e) {
				LOGGER.error("Error while generating an agent package" + e.getMessage());
			}
			return packageFile;
		}
	}

	/**
	 * Create agent package.
	 *
	 * @return File  agent package.
	 */
	public File createAgentPackage() {
		return createAgentPackage(null, null, config.getControllerPort(), null);
	}

	/**
	 * Create agent package.
	 *
	 * @param connectionIP host ip.
	 * @param region       region
	 * @param owner        owner
	 * @return File  agent package.
	 */
	public File createAgentPackage(String region, String connectionIP, int port, String owner) {
		synchronized (AgentPackageService.class) {
			return createPackage(agentPackageHandler, (URLClassLoader) getClass().getClassLoader(), region, connectionIP, port, owner);
		}
	}

	private void addDependentLibToTarStream(PackageHandler packageHandler, TarArchiveOutputStream tarOutputStream, URLClassLoader classLoader) throws IOException {
		if (classLoader == null) {
			classLoader = (URLClassLoader) getClass().getClassLoader();
		}
		packageHandler.addBaseFolderToPackage(tarOutputStream);
		Set<String> libs = packageHandler.getPackageDependentLibs(classLoader);
		packageHandler.copyShellFile(tarOutputStream);

		for (URL eachUrl : classLoader.getURLs()) {
			File eachClassPath = new File(eachUrl.getFile());
			if (!isJar(eachClassPath)) {
				continue;
			}
			if (isDependentLib(eachClassPath, libs)) {
				packageHandler.addFileToPackage(tarOutputStream, eachClassPath, eachClassPath.getName());
			}
		}
	}

	private TarArchiveOutputStream createTarArchiveStream(File packageFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(packageFile);
		return new TarArchiveOutputStream(new BufferedOutputStream(fos));
	}

	/**
	 * Check if this given path is jar.
	 *
	 * @param libFile lib file
	 * @return true if it's jar
	 */
	public boolean isJar(File libFile) {
		return libFile.getName().endsWith(".jar");
	}

	/**
	 * Check if this given lib file in the given lib set.
	 *
	 * @param libFile lib file
	 * @param libs    lib set
	 * @return true if dependent lib
	 */
	private boolean isDependentLib(File libFile, Set<String> libs) {
		if (libFile.getName().contains("grinder-3.9.1.jar")) {
			return false;
		}
		String name = libFile.getName().replace("-SNAPSHOT", "").replace("-GA", "");
		final int libVersionStartIndex = name.lastIndexOf("-");
		name = name.substring(0, (libVersionStartIndex == -1) ? name.lastIndexOf(".") : libVersionStartIndex);
		return libs.contains(name);
	}

	static class TarArchivingZipEntryProcessor implements ZipEntryProcessor {
		private TarArchiveOutputStream tao;
		private FilePredicate filePredicate;
		private String basePath;
		private int mode;

		TarArchivingZipEntryProcessor(TarArchiveOutputStream tao, FilePredicate filePredicate, String basePath, int mode) {
			this.tao = tao;
			this.filePredicate = filePredicate;
			this.basePath = basePath;
			this.mode = mode;
		}

		@Override
		public void process(ZipFile file, ZipEntry entry) throws IOException {
			try (InputStream inputStream = file.getInputStream(entry)) {
				if (filePredicate.evaluate(entry)) {
					addInputStreamToTar(this.tao, inputStream, basePath + FilenameUtils.getName(entry.getName()),
							entry.getSize(),
							this.mode);
				}
			}
		}
	}

}
