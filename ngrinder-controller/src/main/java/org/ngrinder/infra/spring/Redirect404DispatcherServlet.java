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
package org.ngrinder.infra.spring;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * NGrinder specialized Servlet Dispatcher.
 * <p/>
 * The default DispatcherServlet returns 404 error when the corresponding Handler is not found.
 * Then 404 was redirected to corresponding error page using web.xml error_page setting..
 * <p/>
 * This does not work when the 404 error occurs in the SVN path. It should directly return the 404 to the client.
 * So this class checked the path first and redirect the error_404 page directly when the /svn/ path is not included in the request's path info
 */
public class Redirect404DispatcherServlet extends DispatcherServlet {

	private static final String JSON_SUCCESS = "success";
	private static final String JSON_CAUSE = "message";
	private static final UrlPathHelper urlPathHelper = new UrlPathHelper();
	private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

	/**
	 * Redirect to error 404 when the /svn/ is not included in the path.
	 *
	 * @param request  current HTTP requests
	 * @param response current HTTP response
	 * @throws Exception if preparing the response failed
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!request.getPathInfo().startsWith("/svn/")) {
			if (request.getPathInfo().contains("/api")) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.setContentType("application/json; charset=UTF-8");
				String requestUri = urlPathHelper.getRequestUri(request);
				JsonObject object = new JsonObject();

				object.addProperty(JSON_SUCCESS, false);
				object.addProperty(JSON_CAUSE, "API URL " + requestUri + " [" + request.getMethod() + "] does not exist.");
				response.getWriter().write(gson.toJson(object));
				response.flushBuffer();
			} else {
				if (pageNotFoundLogger.isWarnEnabled()) {
					String requestUri = urlPathHelper.getRequestUri(request);
					pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + requestUri +
							"] in DispatcherServlet with name '" + getServletName() + "'");
				}
				response.sendRedirect("/error_404");
			}
		}
	}
}
