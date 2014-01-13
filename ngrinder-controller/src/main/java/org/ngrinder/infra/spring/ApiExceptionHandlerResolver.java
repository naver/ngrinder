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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Api exception handler which emits the exception message in the form of json.
 *
 * @author junoyoon
 * @since 3.2.3
 */
public class ApiExceptionHandlerResolver implements HandlerExceptionResolver, Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandlerResolver.class);
	private static final String JSON_SUCCESS = "success";
	private static final String JSON_CAUSE = "message";
	private static final String JSON_STACKTRACE = "stackTrace";
	private int order;

	private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return order;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.HandlerExceptionResolver#resolveException
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, java.lang.Object,
	 * java.lang.Exception)
	 */
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
	                                     Exception ex) {
		if (!(handler instanceof HandlerMethod)) {
			return null;
		}

		RestAPI methodAnnotation = ((HandlerMethod) handler).getMethodAnnotation(RestAPI.class);
		if (methodAnnotation == null) {
			return null;
		}
		JsonObject object = new JsonObject();
		object.addProperty(JSON_SUCCESS, false);
		object.addProperty(JSON_CAUSE, ex.getMessage());
		StringWriter out = new StringWriter();
		//noinspection ThrowableResultOfMethodCallIgnored
		Throwable throwable = ExceptionUtils.sanitize(ex);
		PrintWriter printWriter = new PrintWriter(out);
		throwable.printStackTrace(printWriter);
		object.addProperty(JSON_STACKTRACE, out.toString());
		IOUtils.closeQuietly(printWriter);
		String jsonMessage = gson.toJson(object);
		try {
			response.setStatus(500);
			response.setContentType("application/json; charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			PrintWriter writer = response.getWriter();
			writer.write(jsonMessage);
			response.flushBuffer();
		} catch (IOException e) {
			LOGGER.error("Exception was occurred while processing api exception.", e);
		}
		return new ModelAndView();
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
