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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtil;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.ngrinder.script.svnkitdav.DAVHandlerExFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;

/**
 * Class description
 * 
 * @author mavlarn
 * @Since 3.0
 */
public class DavSvnControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	DavSvnController svnController;

	@Autowired
	private MockFileEntityRepository fileEntityRepository;

	private void prepareSVN() {
		try {
			File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
			fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
			tempRepo.deleteOnExit();
			File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();
			FileUtils.deleteQuietly(testUserRoot);
			testUserRoot.mkdirs();
			CompressionUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
			testUserRoot.deleteOnExit();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testHandleRequest() throws ServletException, IOException {
		prepareSVN();
		
		//test SC_UNAUTHORIZED
		MockHttpServletRequest req = new MockHttpServletRequest(DAVHandlerExFactory.METHOD_PROPFIND,
				"/svn/" + getTestUser().getUserId());
		req.addHeader("Depth", DAVDepth.DEPTH_ONE);
		HttpServletResponse resp = new MockHttpServletResponse();
		svnController.handleRequest(req, resp);

		req.setPathInfo("/" + getTestUser().getUserId());
		resp = new MockHttpServletResponse();
		svnController.handleRequest(req, resp);
		
	}

	@Test
	public void testHandleError() throws IOException {
		DAVException davE = new DAVException("Test Error", 404, 0);
		HttpServletResponse resp = new MockHttpServletResponse();
		DavSvnController.handleError(davE, resp);
	}

	@Test
	public void testGetServletName() {
		svnController.getServletName();
	}

	@Test
	public void testGetServletContext() {
		svnController.getServletContext();
	}

	@Test
	public void testGetInitParameter() {
		svnController.getInitParameter("param");
	}

	@Test
	public void testGetInitParameterNames() {
		svnController.getInitParameterNames();
	}

}
