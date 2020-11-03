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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ngrinder.common.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Api exception handler which emits the exception message in the form of json.
 *
 * @since 3.2.3
 */
public class ApiExceptionHandlerResolver implements HandlerExceptionResolver, Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandlerResolver.class);
	private static final String JSON_SUCCESS = "success";
	private static final String JSON_CAUSE = "message";
	private static final String JSON_STACKTRACE = "stackTrace";
	private int order;

	@Autowired
	private ObjectMapper objectMapper;

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
	@SuppressWarnings("NullableProblems")
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (!(handler instanceof HandlerMethod)) {
			return null;
		}

		boolean isRestControllerPresent = ((HandlerMethod) handler).getMethod().getDeclaringClass().isAnnotationPresent(RestController.class);
		boolean isResponseBodyPresent = ((HandlerMethod) handler).getMethodAnnotation(ResponseBody.class) != null;
		if (!isRestControllerPresent && !isResponseBodyPresent) {
			return null;
		}

		//noinspection ThrowableResultOfMethodCallIgnored
		Throwable throwable = ExceptionUtils.sanitize(ex);

		StringWriter out = new StringWriter();

		try (PrintWriter printWriter = new PrintWriter(out)) {
			throwable.printStackTrace(printWriter);
		}

		Map<String, Object> jsonResponse = buildMap(
			JSON_SUCCESS, false,
			JSON_CAUSE, ex.getMessage(),
			JSON_STACKTRACE, out.toString()
		);

		try {
			response.setStatus(500);
			response.setContentType("application/json; charset=UTF-8");
			response.addHeader("Cache-control", "no-cache");
			objectMapper.writeValue(response.getWriter(), jsonResponse);
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
