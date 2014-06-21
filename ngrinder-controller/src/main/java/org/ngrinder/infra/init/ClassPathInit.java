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
package org.ngrinder.infra.init;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collection;

/**
 * Initialize Classpath initialization for class filtering .
 *
 * This class is used to prevent javaagent abnormal behavior of grinder agent. grinder agent is run
 * with java agent name grinder-dcr-agent-**.jar but grinder agent can mistakenly take the
 * grinder-dcr-agent-javadoc and sources file as javaagent. So.. This class deletes out the sources
 * and javadoc files of grinder-dcr-agent existing in class path.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class ClassPathInit {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathInit.class);

	/**
	 * Clean up grinder-dcr-agent javadoc and source.
	 */
	@PostConstruct
	public void init() {
		final String systemClasspath = System.getProperty("java.class.path", StringUtils.EMPTY);
		for (String pathEntry : systemClasspath.split(File.pathSeparator)) {
			final File parentFile = ObjectUtils.defaultIfNull(new File(pathEntry).getParentFile(), new File("."));
			final Collection<File> childrenFileList = FileUtils.listFiles(parentFile, new String[]{"jar"}, false);
			for (File candidate : childrenFileList) {
				final String name = candidate.getName();
				if (name.startsWith("grinder-dcr-agent") && (name.contains("javadoc") || name.contains("source"))) {
					if (!candidate.delete()) {
						LOGGER.error("Failed to delete grinder-dcr-agent-javadoc and source");
					}
				}
			}
		}
	}
}
