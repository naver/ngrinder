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
package org.ngrinder.common.util;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_URL;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Utility component which provides various Http Container values.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class HttpContainerContext {
	private static final int DEFAULT_WEB_PORT = 80;
	@Autowired
	private Config config;

	/**
	 * Get current container nGrinder context base path.
	 * <p/>
	 * E.g) if user requests http://hostname:port/context_path/realurl, This
	 * will return http://hostname:port/context_path
	 * <p/>
	 * In case of providing "http.url" property in system.properties file, this
	 * method will return pre-set value.
	 *
	 * @return ngrinder context base path on http request.
	 */
	public String getCurrentContextUrlFromUserRequest() {
		String httpUrl = StringUtils.trimToEmpty(config.getControllerProperties().getProperty(PROP_CONTROLLER_URL));
		// if provided
		if (StringUtils.isNotBlank(httpUrl)) {
			return httpUrl;
		}

		// if empty
		HttpServletRequest request = cast(RequestContextHolder.currentRequestAttributes().resolveReference("request"));
		int serverPort = request.getServerPort();
		// If it's http default port it will ignore the port part.
		// However, if ngrinder is provided in HTTPS.. it can be a problem.
		// FIXME : Later fix above.
		String portString = (serverPort == DEFAULT_WEB_PORT) ? StringUtils.EMPTY : ":" + serverPort;
		return httpUrl + request.getScheme() + "://" + request.getServerName() + portString + request.getContextPath();
	}

	/**
	 * Check the user has unix user agent.
	 *
	 * @return true if unix.
	 */
	public boolean isUnixUser() {
		SecurityContextHolderAwareRequestWrapper request = cast(RequestContextHolder.currentRequestAttributes()
				.resolveReference("request"));
		return !StringUtils.containsIgnoreCase(request.getHeader("User-Agent"), "Win");
	}
}
