package org.ngrinder.script.handler;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.FileEntryRepository;

@SuppressWarnings("deprecation")
public class GroovyMavenProjectScriptHandlerTest {

	@Test
	public void testHandlerMatching() {
		GroovyMavenProjectScriptHandler handler = new GroovyMavenProjectScriptHandler();
		User user = new User("my", "my", "password", Role.ADMIN);
		FileEntryRepository serviceMock = mock(FileEntryRepository.class);
		when(serviceMock.hasOne(user, "/hello/world/pom.xml")).thenReturn(true);
		handler.setFileEntryRepository(serviceMock);

		FileEntry entry = new FileEntry();
		entry.setPath("/hello/world/src/main/java/wow/Global.groovy");
		entry.setCreatedUser(user);
		assertThat(handler.canHandle(entry)).isTrue();

		entry.setPath("/hello/world/src/main/wow/Global.groovy");
		assertThat(handler.canHandle(entry)).isFalse();

		entry.setPath("/hello/world/src/main/java/Global.py");
		assertThat(handler.canHandle(entry)).isFalse();

		when(serviceMock.hasOne(user, "/hello/world/pom.xml")).thenReturn(false);
		entry.setPath("/hello/world/src/main/java/Global.groovy");
		assertThat(handler.canHandle(entry)).isFalse();
	}
}
