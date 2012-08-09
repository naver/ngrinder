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
package org.ngrinder.common.util;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Utility Component which provides various Http Container values;
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class HttpContainerContext {
	@Autowired
	private Config config;

	/**
	 * Get current container nGrinder context base path.
	 * 
	 * E.g) if user requests http://hostname:port/context_path/realurl, This will return
	 * http://hostname:port/context_path
	 * 
	 * In case of providing "http.url" property in system.properties file, this method will return pre-set value.
	 * 
	 * @return ngrinder context base path on http request.
	 */
	public String getCurrentRequestUrlFromUserRequest() {
		String httpUrl = config.getSystemProperties().getProperty("http.url", "");
		// if provided
		if (StringUtils.isNotBlank(httpUrl)) {
			return httpUrl;
		}

		// if empty
		SecurityContextHolderAwareRequestWrapper request = (SecurityContextHolderAwareRequestWrapper) RequestContextHolder
				.currentRequestAttributes().resolveReference("request");
		int serverPort = request.getServerPort();
		// If it's http default port it will ignore the port part.
		// However, if ngrinder is provided in HTTPS.. it can be a problem.
		// FIXME : Later fix above.
		String portString = serverPort == 80 ? "" : ":" + serverPort;
		return new StringBuilder(httpUrl).append(request.getScheme()).append("://").append(request.getServerName())
				.append(portString).append(request.getContextPath()).toString();
	}

	public boolean isUnixUser() {
		SecurityContextHolderAwareRequestWrapper request = (SecurityContextHolderAwareRequestWrapper) RequestContextHolder
				.currentRequestAttributes().resolveReference("request");
		return !StringUtils.containsIgnoreCase(request.getHeader("User-Agent"), "Win");
	}
}
