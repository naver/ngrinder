package net.grinder.scriptengine.groovy;


import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class GroovyExceptionProcessorTest {
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	@Test
	@Ignore
	public void testGroovyExceptionProcessor() {
		GroovyExceptionProcessor processor = new GroovyExceptionProcessor();
		assertThat(processor.isApplicationClass("org.codehaus.groovy.Hello"), is(false));
		assertThat(processor.isApplicationClass("sun.wow"), is(false));
		assertThat(processor.isApplicationClass("my.TestGrinder"), is(true));
		try {
			throw new Exception();
		} catch (Exception e) {
			assertThat(processor.sanitize(e).getStackTrace().length, lessThan(10));
		}
	}
}

