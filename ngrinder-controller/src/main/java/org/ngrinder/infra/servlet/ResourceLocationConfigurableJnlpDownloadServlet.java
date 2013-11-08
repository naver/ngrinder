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
package org.ngrinder.infra.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jnlp.sample.servlet.JnlpDownloadServlet;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.ngrinder.common.util.NoOp;
import org.ngrinder.infra.annotation.RuntimeOnlyController;
import org.ngrinder.infra.config.Config;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletContextAware;

/**
 * {@link JnlpDownloadServlet} which forwards the download request to the home folder. This class is
 * mainly implemented by providing decorating {@link ServletContext}.
 * 
 * @author JunHo Yoon
 */
@RuntimeOnlyController("jnlpDownloadServlet")
public class ResourceLocationConfigurableJnlpDownloadServlet extends JnlpDownloadServlet implements HttpRequestHandler,
				ServletConfig, ServletContextAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLocationConfigurableJnlpDownloadServlet.class);
	/** UID. */
	private static final long serialVersionUID = 2703216836987727946L;

	@Autowired
	private Config config;

	private ServletContext servletContext;

	/**
	 * Wrap the given {@link ServletContext} instance with {@link ServletContextDelegate}, so that
	 * it can redirect {@link ServletContext#getRealPath(String)}
	 * {@link ServletContext#getResource(String)},
	 * {@link ServletContext#getResourceAsStream(String)} to ${NGRINDER_HOME}/download/.
	 * 
	 * @param servletContext
	 *            raw {@link ServletContext}
	 */

	@Override
	public void setServletContext(ServletContext servletContext) {
		if (servletContext instanceof ServletContextDelegate) {
			this.servletContext = servletContext;
		} else {
			this.servletContext = new ServletContextDelegate(servletContext, this.config.getHome()
							.getDownloadDirectory());
		}
	}

	/**
	 * This method name should not be init. If it's init, there will be recursion calls.
	 * 
	 * @throws ServletException
	 *             exception
	 */
	@PostConstruct
	public void initialize() throws ServletException {
		super.init(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
					throws ServletException,
					IOException {
		try {
			// Forward the the request to existing JNLPDownloadServlet.
			LOGGER.debug("JNLP file is downloading : {}", request.getPathInfo());
			if (request.getMethod() == "GET") {
				doGet(request, response);
			} else if (request.getMethod() == "HEAD") {
				doHead(request, response);
			} else {
				doGet(request, response);
			}
		} catch (Exception e) {
			NoOp.noOp();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String name) {
		return initParam.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getServletName()
	 */
	@Override
	public String getServletName() {
		return "jnlpDownloadServlet";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	private Map<String, String> initParam = Maps.newHashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getInitParameterNames()
	 */
	@Override
	public Enumeration<?> getInitParameterNames() {
		return new IteratorEnumeration(initParam.keySet().iterator());
	}
}
