package net.grinder.util;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import net.grinder.lang.groovy.GroovyGrinderClassPathProcessor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrinderClassPathTest {
	private final Logger LOGGER = LoggerFactory.getLogger(GrinderClassPathTest.class);

	@Test
	public void testGroovy() {
		GroovyGrinderClassPathProcessor processor = new GroovyGrinderClassPathProcessor();
		assertThat(
						processor.filterPatchClassPath("/lib/grinder-3.9.1-patch.jar" + File.pathSeparator
										+ "/lib/logback-classic-1.0.0.jar", LOGGER)).isEqualTo(
						"/lib/grinder-3.9.1-patch.jar");
	}
}
