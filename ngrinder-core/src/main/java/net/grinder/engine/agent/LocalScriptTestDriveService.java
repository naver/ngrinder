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

import static org.ngrinder.common.util.NoOp.noOp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.FanOutStreamSender;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.util.Directory;
import net.grinder.util.GrinderClassPathUtils;
import net.grinder.util.NetworkUtil;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Script validation service.
 * 
 * It works on local instead of remote agent. The reason I located this class in ngrinder-core is...
 * some grinder core class doesn't have public access..
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class LocalScriptTestDriveService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalScriptTestDriveService.class);

	/**
	 * Validate script.
	 * 
	 * @param base
	 *            working directory
	 * @param script
	 *            script file
	 * @param eventSynchronisation
	 *            condition for event synchronization
	 * @param securityEnabled
	 *            if security is set ot not.
	 * @param hostString
	 *            hostString
	 * @return File which stores validation result.
	 */
	public File doValidate(File base, File script, Condition eventSynchronisation, boolean securityEnabled,
					String hostString) {
		FanOutStreamSender fanOutStreamSender = null;
		ErrorStreamRedirectWorkerLauncher workerLauncher = null;
		boolean stopByTooMuchExecution = false;
		ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
		File file = new File(base, "validation-0.log");
		try {

			fanOutStreamSender = new FanOutStreamSender(1);
			base.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String extension = FilenameUtils.getExtension(pathname.getName());
					if (extension.startsWith("log")) {
						pathname.delete();
					}
					return true;
				}
			});

			GrinderProperties properties = new GrinderProperties();
			PropertyBuilder builder = new PropertyBuilder(properties, new Directory(base), securityEnabled, hostString,
							NetworkUtil.getLocalHostName());
			properties.setInt("grinder.processes", 1);
			properties.setInt("grinder.threads", 1);
			String grinderJVMClassPath = GrinderClassPathUtils.buildForemostClasspathBasedOnCurrentClassLoader(LOGGER)
							+ File.pathSeparator + builder.buildCustomClassPath(true);
			properties.setProperty("grinder.jvm.classpath", grinderJVMClassPath);
			LOGGER.info("grinder.jvm.classpath  : {} ", grinderJVMClassPath);
			AgentIdentityImplementation agentIdentity = new AgentIdentityImplementation("validation");
			agentIdentity.setNumber(0);
			String newClassPath = GrinderClassPathUtils.buildClasspathBasedOnCurrentClassLoader(LOGGER);
			LOGGER.debug("Validation Class Path " + newClassPath);
			Properties systemProperties = new Properties();
			systemProperties.put("java.class.path", base.getAbsolutePath() + File.pathSeparator + newClassPath);
			Directory workingDirectory = new Directory(base);
			String buildJVMArgumentWithoutMemory = builder.buildJVMArgumentWithoutMemory();
			LOGGER.info("JVM Args : {} ", buildJVMArgumentWithoutMemory);
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
				final long sleeptime = 1000;
				final int maximumWaitingCount = 100;
				int waitingCount = 0;
				while (true) {
					if (workerLauncher.allFinished()) {
						break;
					}
					if (waitingCount++ > maximumWaitingCount) {
						LOGGER.error("Validation should be performed within {}. Stop it forcely", sleeptime
										* waitingCount);
						workerLauncher.destroyAllWorkers();
						stopByTooMuchExecution = true;
						break;
					}
					eventSynchronisation.waitNoInterrruptException(sleeptime);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while executing {}, because:{}", script, e.getMessage());
			LOGGER.info("Error Trace", e);
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
			appendingMessageOn(file, "Validation should be performed within 100 sec. Stop it forcely");
		}
		return file;
	}

	private void appendingMessageOn(File file, String msg) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file, true);
			fileWriter.append("\n\n" + msg);
		} catch (IOException e) {
			LOGGER.error("error during appending validation message", e);
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}
	}
}
