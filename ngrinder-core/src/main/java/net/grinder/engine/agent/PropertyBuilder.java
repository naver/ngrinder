package net.grinder.engine.agent;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.util.Directory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

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
	private final GrinderProperties properties;
	private final Directory baseDirectory;
	private final String hostName;
	private final File classPathBase;
	private final boolean securityEnabled;

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
	 * @param hostName
	 *            host name
	 */
	public PropertyBuilder(GrinderProperties properties, Directory baseDirectory, File nGrinderExecClassPathBase,
					boolean securityEnabled, String hostName) {
		this.properties = checkNotNull(properties);
		this.baseDirectory = checkNotNull(baseDirectory);
		this.classPathBase = checkNotNull(nGrinderExecClassPathBase);
		this.securityEnabled = securityEnabled;
		this.hostName = checkNotEmpty(hostName);
	}

	/**
	 * Build JVM Arguments.
	 * 
	 * @return generated jvm arguments
	 */
	public String buildJVMArgument() {
		StringBuilder jvmArguments = new StringBuilder(properties.getProperty("grinder.jvm.arguments", ""));
		if (securityEnabled) {
			jvmArguments = addSecurityManager(jvmArguments);
			jvmArguments = addCurrentAgentPath(jvmArguments);
			jvmArguments = addConsoleIP(jvmArguments);
			jvmArguments = addDNSIP(jvmArguments);
		}
		jvmArguments = addPythonPathJvmArgument(jvmArguments);
		return addCustomDns(jvmArguments).toString();
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
		jvmArguments.append(" -Dpython.home=");
		if (pythonHome != null) {
			jvmArguments.append(pythonHome);
		} else {
			jvmArguments.append(baseDirectory.getFile().getAbsolutePath());
		}
		jvmArguments.append(" ");
		return jvmArguments;
	}

	private StringBuilder addCurrentAgentPath(StringBuilder jvmArguments) {
		return jvmArguments.append(" -Dngrinder.exec.path=").append(classPathBase.getAbsolutePath()).append(" ");
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
		String etcHost = properties.getProperty("ngrinder.etc.hosts", "");
		if (StringUtils.isNotEmpty(etcHost)) {
			jvmArguments.append(" -Dngrinder.etc.hosts=").append(etcHost).append(",").append(hostName)
							.append(":127.0.0.1,localhost:127.0.0.1")
							.append(" -Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDns ");
		}
		return jvmArguments;
	}

}
