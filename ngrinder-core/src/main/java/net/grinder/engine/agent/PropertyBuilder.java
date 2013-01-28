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
package net.grinder.engine.agent;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.util.Directory;
import net.grinder.util.NetworkUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which is responsible to build custom jvm arguments.
 * 
 * This class aware of security. So it produces the appropriate JVM arguments which works at
 * security env.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class PropertyBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessBuilder.class);
	private final GrinderProperties properties;
	private final Directory baseDirectory;
	private final String hostName;
	private final boolean securityEnabled;
	private final String hostString;

	/**
	 * Constructor.
	 * 
	 * @param properties
	 *            {@link GrinderProperties}
	 * @param baseDirectory
	 *            base directory which the script executes.
	 * @param nGrinderExecClassPathBase
	 *            class path base path.
	 * @param securityEnabled
	 *            true if security enable mode
	 * @param hostString
	 *            hostString
	 * @param hostName
	 *            current host name
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled,
					String hostString, String hostName) {
		this.properties = checkNotNull(properties);
		this.baseDirectory = checkNotNull(baseDirectory);
		this.securityEnabled = securityEnabled;
		this.hostString = hostString;
		this.hostName = checkNotEmpty(hostName);
	}

	/**
	 * Build JVM Arguments.
	 * 
	 * @return generated jvm arguments
	 */
	public String buildJVMArgument() {
		return addMemorySettings(new StringBuilder(buildJVMArgumentWithoutMemory())).toString();
	}

	/**
	 * Build JVM Arguments.
	 * 
	 * @return generated jvm arguments
	 */
	public String buildJVMArgumentWithoutMemory() {
		StringBuilder jvmArguments = new StringBuilder(properties.getProperty("grinder.jvm.arguments", ""));
		if (securityEnabled) {
			jvmArguments = addSecurityManager(jvmArguments);
			jvmArguments = addCurrentAgentPath(jvmArguments);
			jvmArguments = addConsoleIP(jvmArguments);
			jvmArguments = addDNSIP(jvmArguments);
		}
		jvmArguments = addPythonPathJvmArgument(jvmArguments);
		jvmArguments = addCustomDns(jvmArguments);
		jvmArguments = addServerMode(jvmArguments);
		return jvmArguments.toString();
	}

	protected static final long DEFAULT_XMX_SIZE = 500 * 1024 * 1024;
	protected static final long DEFAULT_MIN_XMX_SIZE = 50 * 1024 * 1024;
	protected static final long DEFAULT_MAX_XMX_SIZE = 1024 * 1024 * 1024;

	protected StringBuilder addMemorySettings(StringBuilder jvmArguments) {
		String processCountStr = properties.getProperty("grinder.processes", "1");
		int processCount = NumberUtils.toInt(processCountStr, 1);
		long desirableXmx = DEFAULT_XMX_SIZE; // make 500M as default.
		try {
			// Make a room with 100MB.
			long actualFree = new Sigar().getMem().getActualFree() - (100 * 1024 * 1024);
			// If memory enough..
			desirableXmx = actualFree / processCount;

			if (desirableXmx < (DEFAULT_MIN_XMX_SIZE)) {
				LOGGER.error("There is very few memory availble {}. It's not enough to run test", actualFree);
				desirableXmx = DEFAULT_MIN_XMX_SIZE;
			} else if (desirableXmx > DEFAULT_MAX_XMX_SIZE) {
				desirableXmx = DEFAULT_MAX_XMX_SIZE;
			}
		} catch (SigarException e) {
			LOGGER.error("Error occurs while calculating memory size", e);
			desirableXmx = DEFAULT_XMX_SIZE;
		}
		return jvmArguments.append(" -Xms" + getMemorySizeStr(desirableXmx / 2) + "M -Xmx"
						+ getMemorySizeStr(desirableXmx) + "M ");
	}

	private String getMemorySizeStr(long desirableXmx) {
		return String.valueOf(desirableXmx / (1024 * 1024));
	}

	protected StringBuilder addServerMode(StringBuilder jvmArguments) {
		return jvmArguments.append(" -server ");
	}

	protected StringBuilder addSecurityManager(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Djava.security.manager=org.ngrinder.sm.NGrinderSecurityManager ");
	}

	private String getPath(File file, boolean useAbsolutePath) {
		return useAbsolutePath ? FilenameUtils.normalize(file.getAbsolutePath()) : file.getPath();
	}

	/**
	 * Build custom class path based on the jar files on given base path.
	 * 
	 * @param useAbsolutePath
	 *            true if the class path entries should be represented as absolute path
	 * 
	 * @return classpath string
	 */
	public String buildCustomClassPath(final boolean useAbsolutePath) {
		File baseFile = baseDirectory.getFile();
		File libFolder = new File(baseFile, "lib");
		final StringBuffer customClassPath = new StringBuffer();
		customClassPath.append(getPath(baseFile, useAbsolutePath));
		if (libFolder.exists()) {
			customClassPath.append(File.pathSeparator).append(getPath(new File(baseFile, "lib"), useAbsolutePath));
			libFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".jar")) {
						customClassPath.append(File.pathSeparator)
										.append(getPath(new File(dir, name), useAbsolutePath));
					}
					return true;
				}
			});
		}
		return customClassPath.toString();
	}

	/**
	 * Rebase class path from relative path to absolute path.
	 * 
	 * @param classPath
	 *            class path
	 * @return converted path.
	 */
	public String rebaseCustomClassPath(String classPath) {
		StringBuilder newClassPath = new StringBuilder();
		boolean isFirst = true;
		for (String each : StringUtils.split(classPath, ";:")) {
			File file = new File(baseDirectory.getFile(), each);
			if (!isFirst) {
				newClassPath.append(File.pathSeparator);
			}
			isFirst = false;
			newClassPath.append(FilenameUtils.normalize(file.getAbsolutePath()));
		}
		return newClassPath.toString();
	}

	private StringBuilder addPythonPathJvmArgument(StringBuilder jvmArguments) {
		jvmArguments.append(" -Dpython.path=");
		jvmArguments.append(new File(baseDirectory.getFile(), "lib").getAbsolutePath());
		String pythonPath = System.getenv().get("PYTHONPATH");
		if (pythonPath != null) {
			jvmArguments.append(File.pathSeparator).append(pythonPath);
		}
		String pythonHome = System.getenv().get("PYTHONHOME");
		if (pythonHome != null) {
			jvmArguments.append(" -Dpython.home=");
			jvmArguments.append(pythonHome);
		}
		jvmArguments.append(" ");
		File jythonCache = new File(FileUtils.getTempDirectory(), "jython");
		jythonCache.mkdirs();
		jvmArguments.append(" -Dpython.cachedir=").append(jythonCache.getAbsolutePath()).append(" ");
		return jvmArguments;
	}

	private StringBuilder addCurrentAgentPath(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Dngrinder.exec.path=").append(baseDirectory.getFile()).append(" ");
	}

	private StringBuilder addConsoleIP(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Dngrinder.console.ip=")
						.append(properties.getProperty(GrinderProperties.CONSOLE_HOST, "127.0.0.1")).append(" ");
	}

	private StringBuilder addDNSIP(StringBuilder jvmArguments) {
		@SuppressWarnings("restriction")
		List<?> nameservers = sun.net.dns.ResolverConfiguration.open().nameservers();
		return jvmArguments.append(" -Dngrinder.dns.ip=").append(StringUtils.join(nameservers, ",")).append(" ");
	}

	private StringBuilder addCustomDns(StringBuilder jvmArguments) {
		jvmArguments.append(" -Dngrinder.etc.hosts=").append(hostName).append(":127.0.0.1,localhost:127.0.0.1");
		if (StringUtils.isNotEmpty(hostString)) {
			jvmArguments.append(",").append(rebaseHostString(hostString));
		}
		jvmArguments.append(" -Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDns ");
		return jvmArguments;
	}

	/**
	 * Rebase Host String.. add the missing ip addresses if only host is provided..
	 * 
	 * @param hostString
	 *            host string
	 * @return completed host string.
	 */
	public String rebaseHostString(String hostString) {
		String[] split = StringUtils.split(hostString, ",");
		StringBuilder newHostString = new StringBuilder();
		boolean first = true;
		for (String pair : split) {
			if (!first) {
				newHostString.append(",");
			}
			first = false;
			if (pair.startsWith(":")) {
				newHostString.append(pair);
			} else if (pair.contains(":")) {
				newHostString.append(pair);
			} else {
				boolean eachFirst = true;
				for (InetAddress each : NetworkUtil.getIpsFromHost(pair)) {
					if (!eachFirst) {
						newHostString.append(",");
					}
					newHostString.append(pair).append(":").append(each.getHostAddress());
					eachFirst = false;
				}
			}
		}
		return newHostString.toString();
	}

	void addProperties(String key, String value) {
		this.properties.put(key, value);
	}
}
