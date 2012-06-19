package net.grinder.engine.agent;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import net.grinder.common.GrinderProperties;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.process.WorkerProcessEntryPoint;
import net.grinder.util.Directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhncorp.ngrinder.util.HudsonPluginConfig;
import com.nhncorp.ngrinder.util.SystemProperties;

/**
 * Rewrite from original WorkerProcessCommandLine.
 * We need to add a parameters to let agent start for Husdon test in stand alone.
 *
 */
final class WorkerProcessCommandLine implements CommandLine {
	
	private static final Logger LOG = LoggerFactory.getLogger(WorkerProcessCommandLine.class);
	
	private static final String GRINDER_AGENT_JAR = "grinder-dcr-agent-"
			+ SystemProperties.GRINDER_VER + ".jar";
	private static final String GRINDER_JAR = "grinder-"
			+ SystemProperties.GRINDER_VER + ".jar";

	private final Directory m_workingDirectory;
	private final List<String> m_command;
	private final int m_commandClassIndex;

	public WorkerProcessCommandLine(GrinderProperties properties,
			Properties systemProperties,
			String jvmArguments,
			Directory workingDirectory) throws EngineException {

		LOG.info("workingDirectory of WorkerProcessCommandLine is:{}",
				workingDirectory.getFile());
		m_workingDirectory = workingDirectory;
		m_command = new ArrayList<String>();
		m_command.add(properties.getProperty("grinder.jvm", "java"));
		
		if (jvmArguments != null) {
			// Really should allow whitespace to be escaped/quoted.
			final StringTokenizer tokenizer = new StringTokenizer(jvmArguments);

			while (tokenizer.hasMoreTokens()) {
				final String token = tokenizer.nextToken();

				m_command.add(token);
			}
		}

		final StringBuilder classpath = new StringBuilder();

		final String additionalClasspath = properties
		.getProperty("grinder.jvm.classpath");
		if (additionalClasspath != null) {
			classpath.append(additionalClasspath);
		}

		//not to use system class path. We need to get libraries path of this web app.
		final String systemClasspath = systemProperties
				.getProperty("java.class.path");
		if (systemClasspath != null) {
			if (classpath.length() > 0) {
				classpath.append(File.pathSeparatorChar);
			}
			classpath.append(systemClasspath);
		}
		
		classpath.append(File.pathSeparatorChar);
		
		//add web app class path, otherwise, nGrinder re-written class cannot be found
		URL base = this.getClass().getClassLoader().getResource(
				"grinder.properties");
		File baseFile = new File(base.getFile()).getParentFile();
		
		//class path of web app, should be WEB-INF/classes
		String classesPath = baseFile.getAbsolutePath();
		File webInfoPath = new File(base.getFile()).getParentFile().getParentFile();
		String libPath = webInfoPath.getAbsolutePath()+File.separator+"lib";

		//add grinder-dcr-agent.jar
		final File agent = findAgentJarFile(libPath);
		if (agent != null) {
			try {
				m_command.add("-javaagent:"
						+ workingDirectory.rebaseFile(agent));
			} catch (IOException e) {
				throw new EngineException(e.getMessage(), e);
			}
		}
		
		classpath.append(classesPath);
	        
		classpath.append(File.pathSeparatorChar);
		
		classpath.append(libPath + File.separator + GRINDER_JAR);

		File[] jars = m_workingDirectory.getFile().listFiles(new ScriptJarFilter());

		for (File jar : jars) {
			classpath.append(File.pathSeparator);
			classpath.append(jar);
		}
		if (classpath.length() > 0) {
			m_command.add("-classpath");

			try {
				m_command.add(workingDirectory.rebasePath(classpath.toString()));
			} catch (IOException e) {
				throw new EngineException(e.getMessage(), e);
			}
		}

		m_commandClassIndex = m_command.size();
		m_command.add(WorkerProcessEntryPoint.class.getName());

		// add parameters to access hudson server
		if (HudsonPluginConfig.isNeedToHudson()) {
			m_command.add(HudsonPluginConfig.getHudsonHost());
			m_command.add(String.valueOf(HudsonPluginConfig.getHudsonPort()));
		}
	}

	/**
	 * Package scope for the unit tests.
	 */
	public List<String> getCommandList() {
		return m_command;
	}

	@SuppressWarnings("serial")
	private static final Set<String> s_unquoted = new HashSet<String>() {
		{
			add("-classpath");
			add("-client");
			add("-cp");
			add("-jar");
			add("-server");
		}
	};

	public String toString() {
		final String[] commandArray = getCommandList().toArray(new String[0]);

		final StringBuffer buffer = new StringBuffer(commandArray.length * 10);

		for (int j = 0; j < commandArray.length; ++j) {
			if (j != 0) {
				buffer.append(" ");
			}

			final boolean shouldQuote = j != 0 && j != m_commandClassIndex
				&& !s_unquoted.contains(commandArray[j]);

			if (shouldQuote) {
				buffer.append("'");
			}

			buffer.append(commandArray[j]);

			if (shouldQuote) {
				buffer.append("'");
			}
		}

		return buffer.toString();
	}

	static File findAgentJarFile(String path) {
		File grinderAgtJar = new File(path + File.separator + GRINDER_AGENT_JAR);
		if (grinderAgtJar.exists()) {
			return grinderAgtJar;
		} else {
			return null;
		}
	}

	static class ScriptJarFilter implements FileFilter {
		private final String prefix;

		public ScriptJarFilter(String scriptName) {
			//this.prefix = scriptName + "__";
			this.prefix = scriptName; //now will not filter by script name.
		}

		public ScriptJarFilter() {
			prefix = null;
		}

		@Override
		public boolean accept(File file) {
			if (prefix != null) {
				return (file.getName().endsWith(".class") || file.getName().endsWith(".jar"))
						&& file.getName().startsWith(prefix);
			} else {
				return (file.getName().endsWith(".class") || file.getName().endsWith(".jar"));
			}
		}
	}

	@Override
	public Directory getWorkingDirectory() {
		return m_workingDirectory;
	}
	
	/*
	 * Used to get the libraries path of grinder.
	 * In a web app, it is in WEB-INF/lib
	 */
//	public String getGrinderLibPath () {
//		//slf4j library is in web-inf/lib
//		String logClassName = LOG.getClass().getName();
//		String logClassNamePath = logClassName.replace(".", "/");
//		logClassNamePath = logClassNamePath + ".class";
//		String fullPath = this.getClass().getClassLoader().getResource(logClassNamePath).toString();
//		
//		String webLibPath = fullPath.substring(0, fullPath.indexOf(logClassNamePath));
//		if (webLibPath.indexOf("!") > 0) {
//			//it is a inner path of jar like:
//		}
//		return webLibPath;
//	}
}
