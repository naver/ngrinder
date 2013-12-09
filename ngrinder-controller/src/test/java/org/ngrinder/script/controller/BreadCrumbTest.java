package org.ngrinder.script.controller;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.model.User;
import org.ngrinder.script.service.FileEntryService;

public class BreadCrumbTest {
	@Test
	public void testBreadCrumb() {
		FileEntryController controller = new FileEntryController();
		controller.httpContainerContext = mock(HttpContainerContext.class);
		when(controller.httpContainerContext.getCurrentContextUrlFromUserRequest()).thenReturn("http://helloworld.org/ngrinder");
		User user = new User();
		user.setUserId("admin");
		assertThat(controller.getSvnUrlBreadcrumbs(user, "hello/world"))
				.isEqualTo("<a href='http://helloworld.org/ngrinder/script/list'>http://helloworld.org/ngrinder/svn/admin</a>/<a href='http://helloworld.org/ngrinder/script/list/hello'>hello</a>/<a href='http://helloworld.org/ngrinder/script/list/hello/world'>world</a>");
	}
}
