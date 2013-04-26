package org.ngrinder.script.handler;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.script.model.FileEntry;
import org.springframework.beans.factory.annotation.Autowired;

public class ScriptHandlerFactoryTest extends AbstractNGrinderTransactionalTest {
	@Autowired
	private ScriptHandlerFactory factory;

	@Test
	public void testFactoryCreation() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/hello/world.groovy");
		fileEntry.setCreatedUser(getTestUser());
		assertThat(factory.getHandler(fileEntry)).isInstanceOf(GroovyScriptHandler.class);
		fileEntry.setPath("/hello/world.py");
		assertThat(factory.getHandler(fileEntry)).isInstanceOf(JythonScriptHandler.class);
	}
}
