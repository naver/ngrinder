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

import net.grinder.common.GrinderProperties;
import net.grinder.util.Directory;
import net.grinder.util.NetworkUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static javax.net.ssl.SSLSocketFactory.getDefault;
import static org.ngrinder.common.constants.GrinderConstants.GRINDER_PROP_CONNECTION_RESET;
import static org.ngrinder.common.constants.GrinderConstants.GRINDER_SECURITY_LEVEL_LIGHT;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.SystemInfoUtils.getAvailableMemory;

/**
 * Class which is responsible to build custom jvm arguments.
 * <p/>
 * This class aware of security. So it produces the appropriate JVM arguments
 * which works at security env.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("UnusedReturnValue")
public class PropertyBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessBuilder.class);
	private static final Set<String> DISABLED_SSL_PROTOCOLS = new HashSet<>(singletonList("SSLv2Hello"));

	private final GrinderProperties properties;
	private final Directory baseDirectory;
	private final String hostName;
	private final boolean securityEnabled;
	private final String securityLevel;
	private final String hostString;
	private final boolean server;
	private final boolean useXmxLimit;
	private final String additionalJavaOpt;
	private final boolean enableLocalDNS;


	/**
	 * Constructor with null additional java opt value.
	 *
	 * @param properties        {@link GrinderProperties}
	 * @param baseDirectory     base directory which the script executes.
	 * @param securityEnabled   true if security enable mode
	 * @param hostString        hostString
	 * @param hostName          current host name
	 * @param server            server mode
	 * @param useXmxLimit       true if 1G limit should be enabled
	 * @param enableLocalDNS    true if the local dns should be enabled.
	 * @param additionalJavaOpt additional java option to be provided when invoking agent
	 *                          process
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled, String securityLevel,
						   String hostString, String hostName, boolean server, boolean useXmxLimit, boolean enableLocalDNS, String additionalJavaOpt) {
		this.enableLocalDNS = enableLocalDNS;
		this.properties = checkNotNull(properties);
		this.baseDirectory = checkNotNull(baseDirectory);
		this.securityEnabled = securityEnabled;
		this.securityLevel = securityLevel;
		this.hostString = hostString;
		this.hostName = checkNotEmpty(hostName);
		this.server = server;
		this.useXmxLimit = useXmxLimit;
		this.additionalJavaOpt = additionalJavaOpt;
	}

	/**
	 * Constructor with null additional java opt value.
	 *
	 * @param properties        {@link GrinderProperties}
	 * @param baseDirectory     base directory which the script executes.
	 * @param securityEnabled   true if security enable mode
	 * @param hostString        hostString
	 * @param hostName          current host name
	 * @param server            server mode
	 * @param useXmxLimit       true if 1G limit should be enabled
	 * @param additionalJavaOpt additional java option to be provided when invoking agent
	 *                          process
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled, String securityLevel,
						   String hostString, String hostName, boolean server, boolean useXmxLimit, String additionalJavaOpt) {
		this(properties, baseDirectory, securityEnabled, securityLevel, hostString, hostName, server, useXmxLimit, true, additionalJavaOpt);
	}

	/**
	 * Constructor with null additional java opt value.
	 *
	 * @param properties      {@link GrinderProperties}
	 * @param baseDirectory   base directory which the script executes.
	 * @param securityEnabled true if security enable mode
	 * @param hostString      hostString
	 * @param hostName        current host name
	 * @param server          server mode
	 * @param useXmxLimit     true if 1G limit should be enabled
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled, String securityLevel,
						   String hostString, String hostName, boolean server, boolean useXmxLimit) {
		this(properties, baseDirectory, securityEnabled, securityLevel, hostString, hostName, server, useXmxLimit, null);
	}

	/**
	 * Constructor.
	 *
	 * @param properties      {@link GrinderProperties}
	 * @param baseDirectory   base directory which the script executes.
	 * @param securityEnabled true if security enable mode
	 * @param hostString      hostString
	 * @param hostName        current host name
	 * @param server          server mode
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled,
						   String securityLevel, String hostString, String hostName, boolean server) {
		this(properties, baseDirectory, securityEnabled, securityLevel, hostString, hostName, server, true);
	}

	/**
	 * Constructor.
	 *
	 * @param properties      {@link GrinderProperties}
	 * @param baseDirectory   base directory which the script executes.
	 * @param securityEnabled true if security enable mode
	 * @param hostString      hostString
	 * @param hostName        current host name
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled,
						   String securityLevel, String hostString, String hostName) {
		this(properties, baseDirectory, securityEnabled, securityLevel, hostString, hostName, false);
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
		StringBuilder jvmArguments = new StringBuilder();
		if (securityEnabled) {
			addSecurityManager(jvmArguments);
			addCurrentAgentPath(jvmArguments);
			addConsoleIP(jvmArguments);
			addDnsIP(jvmArguments);
		} else {
			jvmArguments.append(properties.getProperty("grinder.jvm.arguments", ""));
			addNativeLibraryPath(jvmArguments);
		}

		if (properties.getBoolean(GRINDER_PROP_CONNECTION_RESET, false)) {
			jvmArguments.append(" -Dngrinder.connection.reset.on.each.test.run=true ");
		}

		addParam(jvmArguments, properties.getProperty("grinder.param", ""));
		addPythonPathJvmArgument(jvmArguments);
		addCustomDns(jvmArguments);
		addUserDir(jvmArguments);
		addContext(jvmArguments);
		addHttpsProtocols(jvmArguments);
		disableSNIExtension(jvmArguments);

		if (server) {
			addServerMode(jvmArguments);
		}

		if (StringUtils.isNotBlank(additionalJavaOpt)) {
			addAdditionalJavaOpt(jvmArguments);
		}

		String jdkVersion = System.getProperty("java.specification.version");
		if (jdkVersion != null && !jdkVersion.startsWith("1.")) {
			jvmArguments.append(" --add-opens java.base/java.net=ALL-UNNAMED ");
		}

		return jvmArguments.toString();
	}

	protected StringBuilder disableSNIExtension(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Djsse.enableSNIExtension=false ");
	}

	protected StringBuilder addHttpsProtocols(StringBuilder jvmArguments) {
		String[] sslProtocols = {"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};

		try {
			SSLSocket socket = (SSLSocket) getDefault().createSocket();
			sslProtocols = socket.getSupportedProtocols();
		} catch (IOException e) {
			noOp();
		}

		List<String> protocols = new ArrayList<>();
		for (String protocol: sslProtocols) {
			if (DISABLED_SSL_PROTOCOLS.contains(protocol)) {
				continue;
			}
			protocols.add(protocol);
		}

		return jvmArguments
			.append(" -Dhttps.protocols=")
			.append(StringUtils.join(protocols, ","))
			.append(" ");
	}

	protected StringBuilder addContext(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Dngrinder.context=agent ");
	}

	protected StringBuilder addParam(StringBuilder jvmArguments, String param) {
		if (StringUtils.isEmpty(param)) {
			return jvmArguments;
		}
		return jvmArguments.append(" -Dparam=").append(param).append(" ");
	}

	protected StringBuilder addAdditionalJavaOpt(StringBuilder jvmArguments) {
		return jvmArguments.append(" ").append(additionalJavaOpt).append(" ");
	}

	protected StringBuilder addNativeLibraryPath(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Djna.library.path=").append(new File(baseDirectory.getFile(), "/lib"))
				.append(" ");
	}

	protected static final long MIN_PER_PROCESS_MEM_SIZE = 50 * 1024 * 1024;
	protected static final long DEFAULT_MAX_XMX_SIZE = 1024 * 1024 * 1024;

	protected StringBuilder addMemorySettings(StringBuilder jvmArguments) {
		String processCountStr = properties.getProperty("grinder.processes", "1");
		// For compatibility, try both.
		int reservedMemoryUnit = properties.getInt("grinder.reserved.memory", 0);
		if (reservedMemoryUnit == 0) {
			reservedMemoryUnit = properties.getInt("grinder.memory.reserved", 300);
		}

		int reservedMemory = Math.max(reservedMemoryUnit, 0) * 1024 * 1024;
		int processCount = NumberUtils.toInt(processCountStr, 1);
		long desirableXmx; // make 500M as default.

		// Make a free memory room size of reservedMemory.
		long free = getAvailableMemory() - reservedMemory;
		long perProcessTotalMemory = Math.max(free / processCount, MIN_PER_PROCESS_MEM_SIZE);
		desirableXmx = (long) (perProcessTotalMemory * 0.5);
		if (this.useXmxLimit) {
			desirableXmx = Math.min(DEFAULT_MAX_XMX_SIZE, desirableXmx);
		}

		jvmArguments.append(" -Xms").append(getMemorySize(desirableXmx)).append("m -Xmx").append(getMemorySize(desirableXmx)).append("m ");
		return jvmArguments;
	}

	private int getMemorySize(long memoryInByte) {
		return (int) (memoryInByte / (1024 * 1024));
	}

	protected StringBuilder addServerMode(StringBuilder jvmArguments) {
		return jvmArguments.append(" -server ");
	}

	protected StringBuilder addSecurityManager(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Djava.security.manager=").append(getSecurityManagerBySecurityLevel(securityLevel)).append(" ");
	}

	private String getSecurityManagerBySecurityLevel(String securityLevel) {
		if (GRINDER_SECURITY_LEVEL_LIGHT.equalsIgnoreCase(securityLevel)) {
			return "org.ngrinder.sm.NGrinderLightSecurityManager";
		} else {
			return "org.ngrinder.sm.NGrinderSecurityManager";
		}
	}

	private String getPath(File file, boolean useAbsolutePath) {
		return useAbsolutePath ? FilenameUtils.normalize(file.getAbsolutePath()) : file.getPath();
	}

	/**
	 * Build custom class path based on the jar files on given base path.
	 *
	 * @param useAbsolutePath true if the class path entries should be represented as
	 *                        absolute path
	 * @return classpath string
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public String buildCustomClassPath(final boolean useAbsolutePath) {
		File baseFile = baseDirectory.getFile();
		File libFolder = new File(baseFile, "lib");
		final StringBuffer customClassPath = new StringBuffer();
		customClassPath.append(getPath(baseFile, useAbsolutePath));
		if (libFolder.exists()) {
			customClassPath.append(File.pathSeparator).append(getPath(new File(baseFile, "lib"), useAbsolutePath));
			libFolder.list((dir, name) -> {
				if (name.endsWith(".jar")) {
					customClassPath.append(File.pathSeparator)
							.append(getPath(new File(dir, name), useAbsolutePath));
				}
				return true;
			});
		}
		return customClassPath.toString();
	}

	/**
	 * Rebase class path from relative path to absolute path.
	 *
	 * @param classPath class path
	 * @return converted path.
	 */
	public String rebaseUserLibraryClassPath(String classPath) {
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

	@SuppressWarnings("ResultOfMethodCallIgnored")
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

	StringBuilder addDnsIP(StringBuilder jvmArguments) {
		try {
			List<?> dnsServers = NetworkUtils.getDnsServers();
			if (!dnsServers.isEmpty()) {
				return jvmArguments.append(" -Dngrinder.dns.ip=").append(StringUtils.join(dnsServers, ",")).append(" ");
			}
		} catch (Exception e) {
			LOGGER.error("Error while adding DNS IPs for the security mode. This might be occurred by not using " +
					"Oracle JDK : {}", e.getMessage());
		}
		return jvmArguments;
	}

	private StringBuilder addCustomDns(StringBuilder jvmArguments) {
		jvmArguments.append(" -Dngrinder.etc.hosts=").append(hostName).append(":127.0.0.1,localhost:127.0.0.1");
		if (StringUtils.isNotEmpty(hostString)) {
			jvmArguments.append(",").append(rebaseHostString(hostString));
		}
		if (enableLocalDNS) {
			jvmArguments.append(" -Dngrinder.enable.local-dns=true ");
		}
		return jvmArguments;
	}

	private StringBuilder addUserDir(StringBuilder jvmArguments) {
		jvmArguments.append(" -Duser.dir=").append(baseDirectory.getFile().getPath()).append(" ");
		return jvmArguments;
	}

	/**
	 * Rebase Host String.. add the missing ip addresses if only host is
	 * provided..
	 *
	 * @param hostString host string
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
			} else if (securityEnabled) {
				// When the security mode is enabled, we should provide all IPs
				boolean eachFirst = true;
				for (InetAddress each : NetworkUtils.getIpsFromHost(pair)) {
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

	@SuppressWarnings("SameParameterValue")
	void addProperties(String key, String value) {
		this.properties.put(key, value);
	}
}
