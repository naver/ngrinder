package net.grinder.scriptengine.groovy;

import groovy.lang.GroovyClassLoader;

import org.junit.Test;

public class GroovyParseErrorTest {
	@Test
	public void testGroovyParseError() {
		GroovyClassLoader classLoader = new GroovyClassLoader();
		try {
			classLoader.parseClass("class WOW {{}");
		} catch (Exception e) {
			System.out.println(e.getMessage());;
		}
	}
}
