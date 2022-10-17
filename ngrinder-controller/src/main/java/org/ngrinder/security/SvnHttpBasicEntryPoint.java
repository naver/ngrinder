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
package org.ngrinder.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Dynamic Realm returnable HttpBasic {@link AuthenticationEntryPoint}.
 *
 * @since 3.2
 */
@Component("svnHttpBasicEntryPoint")
public class SvnHttpBasicEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, // LB
					AuthenticationException authException) throws IOException {
		// Get the first part of url path and use it as a realm.
		String pathInfo = request.getPathInfo();
		String[] split = StringUtils.split(pathInfo, '/');
		response.addHeader("WWW-Authenticate", "Basic realm=\"" + StringUtils.defaultIfBlank(split[0], "admin") + "\"");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
	}
}
