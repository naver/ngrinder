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
package org.ngrinder.user.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.User;
import org.ngrinder.security.NGrinderUserDetailsService;
import org.ngrinder.security.SecuredUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since 3.1
 */
public class UserContextTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private NGrinderUserDetailsService userDetailService;
	
	@Test
	public void testGetUser() {
		UserContext userCtx = new UserContext();
		
		//in super.beforeSetSecurity(), there is an admin user is set, but the auth is invalid
		try {
			userCtx.getCurrentUser();
			assertTrue(false);
		} catch (AuthenticationCredentialsNotFoundException e) {
			assertTrue(true);
		}

		UserDetails user = userDetailService.loadUserByUsername(getTestUser().getUserId());

		Authentication oriAuth = SecurityContextHolder.getContext().getAuthentication();

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, "123");
		SecurityContextHolder.getContext().setAuthentication(token);
		userCtx.getCurrentUser();
		assertTrue(true);

		SecurityContextHolder.getContext().setAuthentication(oriAuth);
	}
}
