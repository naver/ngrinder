package org.ngrinder.script.handler;

import org.junit.Test;

public class GroovyHandlerTest {
	@Test
	public void testGroovySyntaxError() {
		GroovyScriptHandler handler = new GroovyScriptHandler();
		handler.checkSyntaxErrors("print (( 'hello';");
	}
}
