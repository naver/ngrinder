package org.ngrinder.script.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
