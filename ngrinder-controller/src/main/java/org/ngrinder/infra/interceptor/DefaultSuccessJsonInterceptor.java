package org.ngrinder.infra.interceptor;

import org.ngrinder.common.constant.WebConstants;
import org.ngrinder.common.util.JsonUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

@Component
public class DefaultSuccessJsonInterceptor implements HandlerInterceptor, WebConstants {

	private static final Map<String, Object> SUCCESS_JSON = buildMap(JSON_SUCCESS, true);

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView == null && response.isCommitted() == false) {
			PrintWriter writer = response.getWriter();
			writer.write(JsonUtils.serialize(SUCCESS_JSON));
			response.flushBuffer();
		}
	}

}
