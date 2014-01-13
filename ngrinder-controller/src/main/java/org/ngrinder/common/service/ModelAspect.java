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
package org.ngrinder.common.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.ngrinder.infra.spring.SpringContext;
import org.ngrinder.model.BaseModel;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Aspect to inject the created/modified user and date to the model.
 *
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
@Aspect
@Service
public class ModelAspect {

	public static final String EXECUTION_SAVE = "execution(* org.ngrinder.**.*Service.save*(..))";

	@Autowired
	private UserContext userContext;

	@Autowired
	private SpringContext springContext;

	@Autowired
	private UserRepository userRepository;


	/**
	 * Inject the created/modified user and date to the model. It's only applied
	 * in the servlet context.
	 *
	 * @param joinPoint joint point
	 */
	@Before(EXECUTION_SAVE)
	public void beforeSave(JoinPoint joinPoint) {
		for (Object object : joinPoint.getArgs()) {
			// If the object is base model and it's on the servlet
			// context, It's not executed by task scheduling.
			SpringContext springContext = getSpringContext();
			if (object instanceof BaseModel
					&& (springContext.isServletRequestContext() || springContext.isUnitTestContext())) {
				BaseModel<?> model = (BaseModel<?>) object;
				Date lastModifiedDate = new Date();
				model.setLastModifiedDate(lastModifiedDate);
				User currentUser = userContext.getCurrentUser();
				model.setLastModifiedUser(userRepository.findOne(currentUser.getId()));

				if (!model.exist() || model.getCreatedUser() == null) {
					model.setCreatedDate(lastModifiedDate);
					User factualUser = currentUser.getFactualUser();
					model.setCreatedUser(userRepository.findOne(factualUser.getId()));
				}
			}
		}
	}

	public SpringContext getSpringContext() {
		return springContext;
	}

	public void setSpringContext(SpringContext springContext) {
		this.springContext = springContext;
	}

}
