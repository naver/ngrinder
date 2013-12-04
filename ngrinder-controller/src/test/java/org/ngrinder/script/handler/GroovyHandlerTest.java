package org.ngrinder.script.handler;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GroovyHandlerTest {
	@Test
	public void testGroovySyntaxError() {
		GroovyScriptHandler handler = new GroovyScriptHandler();
		String checkSyntaxErrors = handler.checkSyntaxErrors("hello", "print (( 'hello';");
		assertThat(checkSyntaxErrors, notNullValue());
		checkSyntaxErrors = handler.checkSyntaxErrors("hello", "print 'hello';");
		assertThat(checkSyntaxErrors, nullValue());
	}
}
