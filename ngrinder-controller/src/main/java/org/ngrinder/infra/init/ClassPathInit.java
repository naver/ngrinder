package org.ngrinder.infra.init;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class ClassPathInit {
	@PostConstruct
	public void init() {
		final String systemClasspath = System.getProperty("java.class.path");
		if (systemClasspath != null) {
			deleteNonAgentJarFile(systemClasspath);
		}

	}

	static File deleteNonAgentJarFile(String path) {
		for (String pathEntry : path.split(File.pathSeparator)) {
			final File f = new File(pathEntry).getParentFile();
			final File parentFile = f != null ? f : new File(".");

			final File[] children = parentFile.listFiles();

			if (children != null) {
				for (File candidate : children) {
					final String name = candidate.getName();

					if (name.startsWith("grinder-dcr-agent") && name.endsWith(".jar")
							&& (name.contains("javadoc") || name.contains("source"))) {
						candidate.delete();
					}
				}
			}
		}

		return null;
	}

}
