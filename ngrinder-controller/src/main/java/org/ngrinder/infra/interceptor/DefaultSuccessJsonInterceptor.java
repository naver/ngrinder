package org.ngrinder.infra.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ngrinder.common.constant.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

@Component
public class DefaultSuccessJsonInterceptor implements HandlerInterceptor, WebConstants {

	private static final Map<String, Object> SUCCESS_JSON = buildMap(JSON_SUCCESS, true);

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("NullableProblems")
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView == null && !response.isCommitted() &&
			handler instanceof HandlerMethod && ((HandlerMethod) handler).isVoid()) {
			objectMapper.writeValue(response.getWriter(), SUCCESS_JSON);
			response.flushBuffer();
		}
	}

}
