package net.grinder.scriptengine.groovy;

import groovy.lang.GroovyClassLoader;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GroovyParseErrorTest {
	@Test
	public void testGroovyParseError() {
		GroovyClassLoader classLoader = new GroovyClassLoader();
		try {
			classLoader.parseClass("class WOW {{}");
			fail("Exception should be occurred.");
		} catch (Exception e) {
			assertThat(e.getMessage(), containsString("1: expecting '}'"));
		}
	}
}
