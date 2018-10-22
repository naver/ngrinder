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
import net.grinder.communication.FanOutStreamSender;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.lang.AbstractLanguageHandler;
import net.grinder.lang.Lang;
import net.grinder.util.AbstractGrinderClassPathProcessor;
import net.grinder.util.Directory;
import net.grinder.util.NetworkUtils;
import net.grinder.util.thread.Condition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Script validation service.
 *
 * It works on local instead of remote agent. The reason this class is located
 * in ngrinder-core is... some The Grinder core class doesn't have public
 * access..
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class LocalScriptTestDriveService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalScriptTestDriveService.class);
	private static final int DEFAULT_TIMEOUT = 100;
	private File requiredLibraryDirectory;

	public LocalScriptTestDriveService(File requiredLibraryDirectory) {
		this.requiredLibraryDirectory = requiredLibraryDirectory;
	}

	/**
	 * Validate script with 100 sec timeout.
	 *
	 * @param base                 working directory
	 * @param script               script file
	 * @param eventSynchronisation condition for event synchronization
	 * @param securityEnabled      if security is set or not.
	 * @param hostString           hostString
	 * @return File which stores validation result.
	 */
	public File doValidate(File base, File script, Condition eventSynchronisation, boolean securityEnabled,
						   String securityLevel, String hostString) {
		return doValidate(base, script, eventSynchronisation, securityEnabled, securityLevel, hostString, getDefaultTimeout());
	}

	protected int getDefaultTimeout() {
		return DEFAULT_TIMEOUT;
	}

	/**
	 * Validate script.
	 *
	 * @param base                 working directory
	 * @param script               script file
	 * @param eventSynchronisation condition for event synchronization
	 * @param securityEnabled      if security is set or not.
	 * @param hostString           hostString
	 * @param timeout              timeout in sec.
	 * @return File which stores validation result.
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public File doValidate(File base, File script, Condition eventSynchronisation, boolean securityEnabled,
						   String securityLevel, String hostString, final int timeout) {
		FanOutStreamSender fanOutStreamSender = null;
		ErrorStreamRedirectWorkerLauncher workerLauncher = null;
		boolean stopByTooMuchExecution = false;
		ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
		File file = new File(base, "validation-0.log");
		try {

			fanOutStreamSender = new FanOutStreamSender(1);
			deleteLogs(base);

			AbstractLanguageHandler handler = Lang.getByFileName(script).getHandler();
			AbstractGrinderClassPathProcessor classPathProcessor = handler.getClassPathProcessor();
			GrinderProperties properties = new GrinderProperties();
			PropertyBuilder builder = new ValidationPropertyBuilder(properties, new Directory(base), securityEnabled, securityLevel, hostString,
				NetworkUtils.getLocalHostName());
			properties.setInt("grinder.agents", 1);
			properties.setInt("grinder.processes", 1);
			properties.setInt("grinder.threads", 1);
			properties.setBoolean("grinder.script.validation", true);
			String grinderJVMClassPath = getHomeLibraryPath(classPathProcessor.buildForemostClasspathBasedOnCurrentClassLoader(LOGGER))
				+ File.pathSeparator + getHomeLibraryPath(classPathProcessor.buildPatchClasspathBasedOnCurrentClassLoader(LOGGER))
				+ File.pathSeparator + builder.buildCustomClassPath(true);
			properties.setProperty("grinder.jvm.classpath", grinderJVMClassPath = grinderJVMClassPath.replace(";;", ";"));
			LOGGER.info("grinder.jvm.classpath : {} ", grinderJVMClassPath);
			AgentIdentityImplementation agentIdentity = new AgentIdentityImplementation("validation");
			agentIdentity.setNumber(0);
			String newClassPath = classPathProcessor.buildClasspathBasedOnCurrentClassLoader(LOGGER);
			LOGGER.debug("validation class path " + newClassPath);
			Properties systemProperties = new Properties();

			String path;
			if (isRunningOnWas()) {
				path = getHomeLibraryPath(newClassPath);
			} else {
				path = runtimeClassPath() + newClassPath;
			}
			systemProperties.put("java.class.path", path);

			Directory workingDirectory = new Directory(base);
			String buildJVMArgumentWithoutMemory = builder.buildJVMArgumentWithoutMemory();
			LOGGER.info("jvm args : {} ", buildJVMArgumentWithoutMemory);
			final WorkerProcessCommandLine workerCommandLine = new WorkerProcessCommandLine(properties,
					systemProperties, buildJVMArgumentWithoutMemory, workingDirectory);

			ScriptLocation scriptLocation = new ScriptLocation(workingDirectory, script);
			ProcessWorkerFactory workerFactory = new ProcessWorkerFactory(workerCommandLine, agentIdentity,
					fanOutStreamSender, false, scriptLocation, properties);

			workerLauncher = new ErrorStreamRedirectWorkerLauncher(1, workerFactory, eventSynchronisation, LOGGER,
					byteArrayErrorStream);

			// Start
			workerLauncher.startAllWorkers();
			// Wait for a termination event.
			synchronized (eventSynchronisation) {
				final long sleep = 1000;
				int waitingCount = 0;
				while (true) {
					if (workerLauncher.allFinished()) {
						break;
					}
					if (waitingCount++ > timeout) {
						LOGGER.error("Validation should be performed within {} sec. Stop it by force", timeout);
						workerLauncher.destroyAllWorkers();
						stopByTooMuchExecution = true;
						break;
					}
					eventSynchronisation.waitNoInterrruptException(sleep);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while executing {} because {}", script, e.getMessage());
			LOGGER.info("The error detail is ", e);
			appendingMessageOn(file, ExceptionUtils.getFullStackTrace(e));
		} finally {
			if (workerLauncher != null) {
				workerLauncher.shutdown();
			}
			if (fanOutStreamSender != null) {
				fanOutStreamSender.shutdown();
			}
			// To be safe, wait again..
			int waitingCount = 0;
			while (workerLauncher != null) {
				final int maximumWaitingCount = 10;
				if (workerLauncher.allFinished() || waitingCount++ > maximumWaitingCount) {
					break;
				}
				synchronized (eventSynchronisation) {
					eventSynchronisation.waitNoInterrruptException(1000);
				}
			}

		}
		appendingMessageOn(file, byteArrayErrorStream.toString());
		File errorValidation = new File(base, "error_validation-0.log");
		if (errorValidation.exists()) {
			String errorValidationResult = "";
			try {
				errorValidationResult = FileUtils.readFileToString(errorValidation);
			} catch (IOException e) {
				noOp();
			}
			appendingMessageOn(file, errorValidationResult);
		}
		if (stopByTooMuchExecution) {
			appendingMessageOn(file, "Validation should be performed within " + timeout + " sec. Stop it by force");
		}
		return file;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void deleteLogs(File base) {
		base.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathName) {
				String extension = FilenameUtils.getExtension(pathName.getName());
				if (extension.startsWith("log")) {
					pathName.delete();
				}
				return true;
			}
		});
	}

	private void appendingMessageOn(File file, String msg) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file, true);
			fileWriter.append("\n\n").append(msg);
		} catch (IOException e) {
			LOGGER.error("Error during appending validation messages", e);
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}
	}

	private String getHomeLibraryPath(String classPath) {
		StringBuilder homeLibraryPath = new StringBuilder();
		for (String path : classPath.split(File.pathSeparator)) {
			homeLibraryPath.append(requiredLibraryDirectory.getAbsolutePath()).append(File.separator).append(FilenameUtils.getName(path)).append(File.pathSeparator);
		}
		return homeLibraryPath.toString();
	}

	private boolean isRunningOnWas() {
		return ((URLClassLoader) LocalScriptTestDriveService.class.getClassLoader()).getURLs()[0].getProtocol().equals("jar");
	}

	private String runtimeClassPath() {
		StringBuilder runtimeClassPath = new StringBuilder();
		for (URL url : ((URLClassLoader) LocalScriptTestDriveService.class.getClassLoader()).getURLs()) {
			if (url.getPath().contains("ngrinder-runtime") || url.getPath().contains("ngrinder-groovy")) {
				runtimeClassPath.append(url.getFile()).append(File.pathSeparator);
			}
		}
		return runtimeClassPath.toString();
	}
}
