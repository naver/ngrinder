package org.ngrinder.script.service;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
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

	@Test
	public void testFileNameFromUrl() {
		assertThat(fileEntryService.getTestNameFromUrl("http://helloworld"), is("test_for_helloworld"));
		assertThat(fileEntryService.getTestNameFromUrl("http://helloworld.com"),
						is("test_for_helloworld_com"));
		assertThat(fileEntryService.getTestNameFromUrl("http://helloworld.com/wewe.nhn"),
						is("test_for_helloworld_com_wewe_nhn"));
		assertThat(fileEntryService.getTestNameFromUrl("http://helloworld.com/wewe.nhn?wow=%dd"),
						is("test_for_helloworld_com_wewe_nhn"));
	}

	@Test(expected = NGrinderRuntimeException.class)
	public void testFileNameFromInvalidUrl() {
		fileEntryService.getTestNameFromUrl("htt22p://helloworld22");
	}

}
