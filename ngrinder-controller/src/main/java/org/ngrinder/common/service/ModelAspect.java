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

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.ngrinder.infra.spring.SpringContext;
import org.ngrinder.model.BaseModel;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserContext;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static java.time.Instant.now;
import static org.ngrinder.user.repository.UserSpecification.idEqual;

/**
 * Aspect to inject the created/modified user and date to the model.
 *
 * @since 3.0
 */
@Aspect
@Service
@RequiredArgsConstructor
public class ModelAspect {

	private static final String EXECUTION_SAVE = "execution(* org.ngrinder.**.*Service.save*(..))";

	private final UserContext userContext;

	private final SpringContext springContext;

	private final UserRepository userRepository;

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
			if (object instanceof BaseModel
					&& (springContext.isAuthenticationContext() || springContext.isUnitTestContext())) {
				BaseModel<?> model = (BaseModel<?>) object;
				Instant lastModifiedAt = now();
				model.setLastModifiedAt(lastModifiedAt);

				User currentUser = userContext.getCurrentUser();
				long currentUserId = currentUser.getId();

				model.setLastModifiedBy(userRepository.findOne(idEqual(currentUserId))
					.orElseThrow(() -> new IllegalArgumentException("No user found with id : " + currentUserId)));

				if (!model.exist() || model.getCreatedBy() == null) {
					long factualUserId = currentUser.getFactualUser().getId();
					model.setCreatedAt(lastModifiedAt);
					model.setCreatedBy(userRepository.findOne(idEqual(factualUserId))
						.orElseThrow(() -> new IllegalArgumentException("No user found with id : " + factualUserId)));
				}
			}
		}
	}

}
