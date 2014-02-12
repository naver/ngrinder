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

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletContextAware;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVHandlerFactory;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVResponse;
import org.tmatesoft.svn.core.internal.server.dav.handlers.ServletDAVHandler;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * WebDav servlet implementation on SVN Server. This servlet translates WEBDAV request into
 * underlying SVN Repo.
 * <p/>
 * This implementation is borrowed from SVNKit-DAV project.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Controller("svnDavServlet")
public class SvnDavController implements HttpRequestHandler, ServletConfig, ServletContextAware {

	public static final Logger LOGGER = LoggerFactory.getLogger(SvnDavController.class);
	public static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";

	@Autowired
	private Config config;

	/**
	 * Initialize. Set the SVNParentPath as $(NGRINDER_HOME)/repos
	 */
	@PostConstruct
	public void init() {
		initParam.put("SVNParentPath", config.getHome().getRepoDirectoryRoot().getAbsolutePath());
		FSRepositoryFactory.setup();
		try {
			myDAVConfig = new DAVConfig(getServletConfig());
		} catch (SVNException e) {
			myDAVConfig = null;
		}
	}

	private Map<String, String> initParam = new HashMap<String, String>();
	private DAVConfig myDAVConfig;
	private ServletContext servletContext;

	protected DAVConfig getDAVConfig() {
		return myDAVConfig;
	}

	public void setDAVConfig(DAVConfig davConfig) {
		this.myDAVConfig = davConfig;
	}

	/**
	 * Returns this servlet's {@link ServletConfig} object.
	 *
	 * @return ServletConfig the <code>ServletConfig</code> object that initialized this servlet
	 */

	public ServletConfig getServletConfig() {
		return this;
	}

	/**
	 * Request Handler.
	 *
	 * @param request  request
	 * @param response response
	 * @throws ServletException occurs when servlet has a problem.
	 * @throws IOException      occurs when file system has a problem.
	 */
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException,
			IOException {

		if (LOGGER.isTraceEnabled()) {
			logRequest(request);
		}
		try {
			// To make it understand Asian Language..
			request = new MyHttpServletRequestWrapper(request);
			DAVRepositoryManager repositoryManager = new DAVRepositoryManager(getDAVConfig(), request);
			ServletDAVHandler handler = DAVHandlerFactory.createHandler(repositoryManager, request, response);
			handler.execute();
		} catch (DAVException de) {
			response.setContentType(XML_CONTENT_TYPE);
			handleError(de, response);
		} catch (SVNException svne) {
			StringWriter sw = new StringWriter();
			svne.printStackTrace(new PrintWriter(sw));

			/**
			 * truncate status line if it is to long
			 */
			String msg = sw.getBuffer().toString();
			if (msg.length() > 128) {
				msg = msg.substring(0, 128);
			}
			SVNErrorCode errorCode = svne.getErrorMessage().getErrorCode();
			if (errorCode == SVNErrorCode.FS_NOT_DIRECTORY || errorCode == SVNErrorCode.FS_NOT_FOUND
					|| errorCode == SVNErrorCode.RA_DAV_PATH_NOT_FOUND) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
			} else if (errorCode == SVNErrorCode.NO_AUTH_FILE_PATH) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
			} else if (errorCode == SVNErrorCode.RA_NOT_AUTHORIZED) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
			} else {
				String errorBody = generateStandardizedErrorBody(errorCode.getCode(), null, null, svne.getMessage());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType(XML_CONTENT_TYPE);
				response.getWriter().print(errorBody);
			}
		} catch (Throwable th) {
			StringWriter sw = new StringWriter();
			th.printStackTrace(new PrintWriter(sw));
			String msg = sw.getBuffer().toString();
			LOGGER.debug("Error in DavSVN Controller", th);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
		} finally {
			response.flushBuffer();
		}
	}

	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	private void logRequest(HttpServletRequest request) {
		StringBuilder logBuffer = new StringBuilder();
		logBuffer.append('\n');
		logBuffer.append("request.getAuthType(): " + request.getAuthType());
		logBuffer.append('\n');
		logBuffer.append("request.getCharacterEncoding(): " + request.getCharacterEncoding());
		logBuffer.append('\n');
		logBuffer.append("request.getContentType(): " + request.getContentType());
		logBuffer.append('\n');
		logBuffer.append("request.getContextPath(): " + request.getContextPath());
		logBuffer.append('\n');
		logBuffer.append("request.getContentLength(): " + request.getContentLength());
		logBuffer.append('\n');
		logBuffer.append("request.getMethod(): " + request.getMethod());
		logBuffer.append('\n');
		logBuffer.append("request.getPathInfo(): " + request.getPathInfo());
		logBuffer.append('\n');
		logBuffer.append("request.getPathTranslated(): " + request.getPathTranslated());
		logBuffer.append('\n');
		logBuffer.append("request.getQueryString(): " + request.getQueryString());
		logBuffer.append('\n');
		logBuffer.append("request.getRemoteAddr(): " + request.getRemoteAddr());
		logBuffer.append('\n');
		logBuffer.append("request.getRemoteHost(): " + request.getRemoteHost());
		logBuffer.append('\n');
		logBuffer.append("request.getRemoteUser(): " + request.getRemoteUser());
		logBuffer.append('\n');
		logBuffer.append("request.getRequestURI(): " + request.getRequestURI());
		logBuffer.append('\n');
		logBuffer.append("request.getServerName(): " + request.getServerName());
		logBuffer.append('\n');
		logBuffer.append("request.getServerPort(): " + request.getServerPort());
		logBuffer.append('\n');
		logBuffer.append("request.getServletPath(): " + request.getServletPath());
		logBuffer.append('\n');
		logBuffer.append("request.getRequestURL(): " + request.getRequestURL());
		LOGGER.trace(logBuffer.toString());
	}

	/**
	 * Handler for error.
	 *
	 * @param error           error occurred
	 * @param servletResponse response
	 * @throws IOException occurs when IO has problem
	 */
	public static void handleError(DAVException error, HttpServletResponse servletResponse) throws IOException {
		SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, error);
		DAVResponse response = error.getResponse();
		if (response == null) {
			DAVException stackErr = error;
			while (stackErr != null && stackErr.getTagName() == null) {
				stackErr = stackErr.getPreviousException();
			}

			if (stackErr != null && stackErr.getTagName() != null) {
				servletResponse.setContentType(XML_CONTENT_TYPE);
				servletResponse.setStatus(stackErr.getResponseCode());

				StringBuffer errorMessageBuffer = new StringBuffer();
				SVNXMLUtil.addXMLHeader(errorMessageBuffer);
				errorMessageBuffer.append('\n');
				errorMessageBuffer.append("<D:error xmlns:D=\"DAV:\"");

				if (stackErr.getMessage() != null) {
					errorMessageBuffer.append(" xmlns:m=\"http://apache.org/dav/xmlns\"");
				}

				if (stackErr.getNameSpace() != null) {
					errorMessageBuffer.append(" xmlns:C=\"");
					errorMessageBuffer.append(stackErr.getNameSpace());
					errorMessageBuffer.append("\">\n<C:");
					errorMessageBuffer.append(stackErr.getTagName());
					errorMessageBuffer.append("/>");
				} else {
					errorMessageBuffer.append(">\n<D:");
					errorMessageBuffer.append(stackErr.getTagName());
					errorMessageBuffer.append("/>");
				}

				if (stackErr.getMessage() != null) {
					errorMessageBuffer.append("<m:human-readable errcode=\"");
					errorMessageBuffer.append(stackErr.getErrorID());
					errorMessageBuffer.append("\">\n");
					errorMessageBuffer.append(SVNEncodingUtil.xmlEncodeCDATA(stackErr.getMessage()));
					errorMessageBuffer.append('\n');
					errorMessageBuffer.append("</m:human-readable>\n");
				}
				errorMessageBuffer.append("</D:error>\n");
				servletResponse.getWriter().print(errorMessageBuffer.toString());
				SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, errorMessageBuffer.toString());
				return;
			}
			servletResponse.setStatus(error.getResponseCode());
			return;
		}

		DAVXMLUtil.sendMultiStatus(response, servletResponse, error.getResponseCode(), null);
	}

	private String generateStandardizedErrorBody(int errorID, String namespace, String tagName, String description) {
		StringBuffer xmlBuffer = new StringBuffer();
		SVNXMLUtil.addXMLHeader(xmlBuffer);

		Collection namespaces = new ArrayList();
		namespaces.add(DAVElement.DAV_NAMESPACE);
		namespaces.add(DAVElement.SVN_APACHE_PROPERTY_NAMESPACE);
		if (namespace != null) {
			namespaces.add(namespace);
		}
		SVNXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVXMLUtil.SVN_DAV_ERROR_TAG,
				namespaces, SVNXMLUtil.PREFIX_MAP, xmlBuffer);
		String prefix = (String) SVNXMLUtil.PREFIX_MAP.get(namespace);
		if (prefix != null) {
			prefix = SVNXMLUtil.DAV_NAMESPACE_PREFIX;
		}
		if (tagName != null && tagName.length() > 0) {
			SVNXMLUtil.openXMLTag(prefix, tagName, SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, xmlBuffer);
		}

		SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_APACHE_PROPERTY_PREFIX, "human-readable", SVNXMLUtil.XML_STYLE_NORMAL,
				"errcode", String.valueOf(errorID), xmlBuffer);
		xmlBuffer.append(SVNEncodingUtil.xmlEncodeCDATA(description));
		SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_APACHE_PROPERTY_PREFIX, "human-readable", xmlBuffer);
		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVXMLUtil.SVN_DAV_ERROR_TAG, xmlBuffer);
		return xmlBuffer.toString();
	}

	@Override
	public String getServletName() {
		return "svnDavServlet";
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public String getInitParameter(String name) {
		return initParam.get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return new IteratorEnumeration(initParam.keySet().iterator());
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
