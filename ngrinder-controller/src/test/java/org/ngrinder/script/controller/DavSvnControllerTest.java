/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.script.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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

	@Test
	public void testHandleRequest() throws ServletException, IOException {
		HttpServletRequest req = new MockHttpServletRequest(getTestUser().getUserId(), "");
		HttpServletResponse resp = new MockHttpServletResponse();
		svnController.handleRequest(req, resp);
	}

	@Test
	public void testHandleError() throws IOException {
		DAVException davE = new DAVException("Test Error", 404, 0);
		HttpServletResponse resp = new MockHttpServletResponse();
		DavSvnController.handleError(davE, resp);
	}

	/**
	 * Test method for {@link org.ngrinder.script.controller.DavSvnController#getSharedActivity()}.
	 */
	@Test
	public void testGetSharedActivity() {
		DavSvnController.getSharedActivity();
	}

	/**
	 * Test method for {@link org.ngrinder.script.controller.DavSvnController#setSharedActivity(java.lang.String)}.
	 */
	@Test
	public void testSetSharedActivity() {
		DavSvnController.setSharedActivity("TEST SA");
	}

	/**
	 * Test method for {@link org.ngrinder.script.controller.DavSvnController#isHTTPServerError(int)}.
	 */
	@Test
	public void testIsHTTPServerError() {
		assertThat(DavSvnController.isHTTPServerError(404), is(false));
		assertThat(DavSvnController.isHTTPServerError(550), is(true));
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
