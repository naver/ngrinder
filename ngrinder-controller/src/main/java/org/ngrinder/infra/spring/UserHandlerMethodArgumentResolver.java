package org.ngrinder.infra.spring;

import org.ngrinder.model.User;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component("userHandlerMethodArgumentResolver")
public class UserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private UserContext userContext;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(User.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return getUserContext().getCurrentUser();
	}

	public UserContext getUserContext() {
		return userContext;
	}

	public void setUserContext(UserContext userContext) {
		this.userContext = userContext;
	}
}
