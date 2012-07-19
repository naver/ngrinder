package org.ngrinder.script.service;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.model.User;

public class FileEntryServiceTest {

	private FileEntryService fileEntryService = new FileEntryService();

	@Test
	public void testFileTemplate() {
		User user = new User();
		user.setUserName("JunHo Yoon");
		String content = fileEntryService.loadFreeMarkerTemplate(user, "http://helloworld/myname/is");
		assertThat(content, containsString("JunHo Yoon"));
		assertThat(content, containsString("http://helloworld/myname/is"));

	}
}
