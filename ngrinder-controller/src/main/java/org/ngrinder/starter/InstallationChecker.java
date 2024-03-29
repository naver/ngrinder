/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.starter;

import com.sun.jna.Platform;

import java.io.File;
import java.util.List;

import static java.lang.System.*;
import static org.ngrinder.common.constant.ConsoleColorConstants.CONSOLE_COLOR_RESET;
import static org.ngrinder.common.constant.ConsoleColorConstants.CONSOLE_COLOR_YELLOW;
import static org.ngrinder.script.handler.GroovyGradleProjectScriptHandler.GRADLE_HOME_ENV_NAME;
import static org.ngrinder.script.handler.GroovyMavenProjectScriptHandler.MAVEN_HOME_ENV_NAME;
import static oshi.util.ExecutingCommand.runNative;

/**
 *
 * Check If specific program is installed.
 *
 * @since 3.5.3
 *
 * */
public enum InstallationChecker {

	MAVEN(MAVEN_HOME_ENV_NAME, Platform.isWindows() ? "mvn.cmd -version" : "mvn -version",
		"Maven isn't installed, You can't run Maven groovy scripts. Please install Maven and set MAVEN_HOME.    "),
	GRADLE(GRADLE_HOME_ENV_NAME, Platform.isWindows() ? "gradle.bat -version" : "gradle -version",
		"Gradle isn't installed, You can't run Gradle groovy scripts. Please install Gradle and set GRADLE_HOME.");

	private final String homeEnvName;
	private final String installationCheckingCommand;
	private final String warningMessage;

	InstallationChecker(String homeEnvName, String installationCheckingCommand, String warningMessage) {
		this.homeEnvName = homeEnvName;
		this.warningMessage = warningMessage;
		this.installationCheckingCommand = installationCheckingCommand;
	}

	public static void checkAll() {
		for (InstallationChecker installationChecker : InstallationChecker.values()) {
			if (!installationChecker.isInstalled()) {
				installationChecker.printWarningMessage();
			}
		}
	}

	private boolean isInstalled() {
		String env = getenv(homeEnvName);
		String homePath = env == null ? "" : env + File.separator +  "bin" + File.separator;
		String checkCommand = homePath + installationCheckingCommand;
		List<String> result = runNative(checkCommand.split(" "), null);
		return !result.isEmpty();
	}

	private void printWarningMessage() {
		out.print(CONSOLE_COLOR_YELLOW);
		out.println("####################################################################################################################");
		out.println("#                                                                                                                  #");
		out.println("# WARNING: " + warningMessage + " #");
		out.println("#                                                                                                                  #");
		out.println("####################################################################################################################");
		out.print(CONSOLE_COLOR_RESET);
	}

}
