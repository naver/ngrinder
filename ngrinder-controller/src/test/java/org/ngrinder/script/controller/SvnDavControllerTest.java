/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.script.controller;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


public class SvnDavControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	SvnDavController svnController;

	@Autowired
	private MockFileEntityRepository fileEntityRepository;

	private void prepareSVN() throws IOException, SVNException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
		fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
		tempRepo.deleteOnExit();
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();
		FileUtils.deleteQuietly(testUserRoot);
		testUserRoot.mkdirs();
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();
		MockServletConfig servletConfig = new MockServletConfig();
		servletConfig.addInitParameter("SVNParentPath", testUserRoot.getAbsolutePath());
		DAVConfig davConfig = new DAVConfig(servletConfig);
		svnController.setDAVConfig(davConfig);
	}


	@Test
	public void testHandleError() throws IOException {
		DAVException davE = new DAVException("Test Error", 404, 0);
		HttpServletResponse resp = new MockHttpServletResponse();
		SvnDavController.handleError(davE, resp);
	}

	@Test
	public void testDavServletEnv() {
		assertThat(svnController.getServletName(), is("svnDavServlet"));

		ServletContext context = new MockServletContext();
		svnController.setServletContext(context);
		assertThat(svnController.getServletContext(), is(context));

		assertThat(svnController.getInitParameter("param"), nullValue());
		assertThat(svnController.getInitParameterNames().nextElement(), is((Object) "SVNParentPath"));
	}


}
