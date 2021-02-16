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
package org.ngrinder.script.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.getenv;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Paths.get;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.UrlUtils.getHost;
import static org.ngrinder.script.model.FileCategory.SCRIPT;
import static org.ngrinder.script.model.FileType.DIR;

/**
 * Groovy gradle project {@link ScriptHandler}.
 *
 * @since 3.5.3
 */
@Component
public class GroovyGradleProjectScriptHandler extends GroovyProjectScriptHandler {

	public static final String GRADLE_HOME_ENV_NAME = "GRADLE_HOME";
	private static final String PATH_SCRIPT_TEMPLATE_DIRECTORY = "/groovy_gradle";

	private final String ngrinderHomePath;
	private String gradlePath;

	public GroovyGradleProjectScriptHandler(Config config) {
		super("groovy_gradle", "", "Groovy Gradle Project", "groovy", "build.gradle", true);

		gradlePath = getSafe(getenv(GRADLE_HOME_ENV_NAME), "");
		if (isNotEmpty(gradlePath)) {
			gradlePath += "/bin/";
		}

		ngrinderHomePath = config.getHome().getDirectory().getAbsolutePath();
	}

	@Override
	protected String getCopyDependenciesCommand(File distDir) {
		String distDirPath = distDir.getAbsolutePath();
		return gradlePath + "gradle -I " + ngrinderHomePath + "/init.gradle -p" + distDirPath +
			" __copyDependencies -PoutputDirectory=" + distDirPath + "/lib";
	}

	@Override
	protected boolean isSuccess(List<String> results) {
		if (results.isEmpty()) {
			return false;
		}
		return results.stream().noneMatch(str -> str.contains("FAILED"));
	}

	@Override
	public boolean prepareScriptEnv(User user, String path, String fileName, String name, // LF
									String url, boolean createLib, String scriptContent) {
		path = PathUtils.join(path, fileName);
		// Create Dir entry
		createBaseDirectory(user, path);

		// Create each template entries
		try {
			createFileEntries(user, path, name, url, scriptContent);
			if (createLib) {
				createLibraryDirectory(user, path);
			}
		} catch (NGrinderRuntimeException e) {
			getFileEntryRepository().delete(user, new ArrayList<>(singletonList(path)));
			throw e;
		}
		return false;
	}

	@Override
	protected void deleteUnnecessaryFilesFromDist(File distDir) {
		super.deleteUnnecessaryFilesFromDist(distDir);
		deleteQuietly(new File(distDir, ".gradle"));
	}

	private void createFileEntries(User user, String path, String name, String url, String scriptContent) {
		String[] scriptTemplatePaths = { getBuildScriptName(), "src/main/resources/resource1.txt", "src/main/java/TestRunner.groovy" };
		String homeScriptTemplateDirectoryPath = getConfig().getHomeScriptTemplateDirectory().getAbsolutePath() + PATH_SCRIPT_TEMPLATE_DIRECTORY;
		for (String scriptTemplatePath : scriptTemplatePaths) {
			try (InputStream inputStream = FileUtils.openInputStream(new File(homeScriptTemplateDirectoryPath + "/" + scriptTemplatePath))) {
				String fileContent = IOUtils.toString(inputStream, UTF_8.name());
				if (scriptTemplatePath.endsWith("TestRunner.groovy")) {
					fileContent = scriptContent;
				} else {
					fileContent = fileContent.replace("${userName}", user.getUserName());
					fileContent = fileContent.replace("${name}", name);
					fileContent = fileContent.replace("${url}", url);
				}
				FileEntry fileEntry = new FileEntry();
				fileEntry.setContent(fileContent);
				fileEntry.setPath(normalize(PathUtils.join(path, scriptTemplatePath), true));
				fileEntry.setDescription("create groovy gradle project");
				String hostName = getHost(url);
				if (isNotEmpty(hostName)
					&& fileEntry.getFileType().getFileCategory() == SCRIPT) {
					fileEntry.getProperties().put("targetHosts", getHost(url));
				} else {
					fileEntry.getProperties().put("targetHosts", EMPTY);
				}
				getFileEntryRepository().save(user, fileEntry, UTF_8.name());
			} catch (IOException e) {
				throw processException("Error while saving " + get(scriptTemplatePath).getFileName(), e);
			}
		}
	}

	private void createBaseDirectory(User user, String path) {
		FileEntry dirEntry = new FileEntry();
		dirEntry.setPath(path);
		// Make it eclipse default folder ignored.
		dirEntry.setProperties(buildMap("svn:ignore", ".project\n.classpath\n.settings\ntarget"));
		dirEntry.setFileType(DIR);
		dirEntry.setDescription("create groovy gradle project");
		getFileEntryRepository().save(user, dirEntry, null);
	}
}
