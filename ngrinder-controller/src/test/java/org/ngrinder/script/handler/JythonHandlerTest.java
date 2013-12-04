package org.ngrinder.script.handler;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JythonHandlerTest {
	@Test
	public void testJythonSyntaxError() {
		JythonScriptHandler handler = new JythonScriptHandler();
		String checkSyntaxErrors = handler.checkSyntaxErrors("hello", "aa print 'hello'");
		assertThat(checkSyntaxErrors, notNullValue());
		checkSyntaxErrors = handler.checkSyntaxErrors("hello", "print 'hello';");
		assertThat(checkSyntaxErrors, nullValue());
	}
}
