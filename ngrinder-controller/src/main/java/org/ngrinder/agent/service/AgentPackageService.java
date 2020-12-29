package org.ngrinder.agent.service;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.packages.AgentPackageHandler;
import org.ngrinder.packages.PackageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import static net.grinder.util.AbstractGrinderClassPathProcessor.getClassPaths;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.util.EncodingUtils.decodePathWithUTF8;
import static org.ngrinder.common.util.StringUtils.replaceLast;

/**
 * Agent package service.
 *
 * @since 3.3
 */
@Service
@RequiredArgsConstructor
public class AgentPackageService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentPackageService.class);

	private final Config config;

	private final AgentPackageHandler agentPackageHandler;

	/**
	 * Create package from PackageHandler.
	 *
	 * @return File package.
	 */
	public File createPackage(PackageHandler packageHandler, String regionName, String connectionIP, int port, String owner) {
		synchronized (AgentPackageService.class) {
			File packageFile = packageHandler.getPackageFile(regionName, connectionIP, owner, false);
			if (packageFile.exists()) {
				return packageFile;
			}
			FileUtils.deleteQuietly(packageFile);
			try (TarArchiveOutputStream tarOutputStream = createTarArchiveStream(packageFile)) {
				addDependentLibToTarStream(packageHandler, tarOutputStream);
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
			return createPackage(agentPackageHandler, region, connectionIP, port, owner);
		}
	}

	private void addDependentLibToTarStream(PackageHandler packageHandler, TarArchiveOutputStream tarOutputStream) throws IOException {
		packageHandler.addBaseFolderToPackage(tarOutputStream);
		Set<String> libs = packageHandler.getPackageDependentLibs();
		packageHandler.copyShellFile(tarOutputStream);

		for (URL eachUrl : getClassPaths(getClass().getClassLoader())) {
			File eachClassPath = new File(decodePathWithUTF8(eachUrl.getFile()));
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
	protected boolean isDependentLib(File libFile, Set<String> libs) {
		if (libFile.getName().contains("grinder-3.9.1.jar")) {
			return false;
		}

		// Replace release types (-SNAPSHOT, -GA, -p1, ...)
		String name = replaceLast(libFile.getName(), "-[a-zA-Z][a-zA-Z1-9]+\\.", ".");
		final int libVersionStartIndex = name.lastIndexOf("-");
		name = name.substring(0, (libVersionStartIndex == -1) ? name.lastIndexOf(".") : libVersionStartIndex);
		return libs.contains(name);
	}
}
