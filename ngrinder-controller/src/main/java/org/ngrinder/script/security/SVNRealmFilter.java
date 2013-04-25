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
package org.ngrinder.script.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since 3.2
 */
public class SVNRealmFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SVNRealmFilter.class);

	@Autowired
	private UserContext userContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
					throws IOException,
					ServletException {
		LOGGER.debug("In SVNRealmFilter.doFilter()");
		HttpServletRequest httpReq = (HttpServletRequest) request;
		String pathInfo = httpReq.getPathInfo();
		final String head = DAVPathUtil.head(pathInfo);
		final User currentUser = userContext.getCurrentUser();
		// check the security. If the other user tries to the other user's
		// repo, deny it.
		if (!StringUtils.equals(currentUser.getUserId(), head)) {
			throw new BadCredentialsException("User " + currentUser.getUserId() + " cannot access repository of "
							+ head);
		}
		chain.doFilter(request, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// Do nothing
	}

}
