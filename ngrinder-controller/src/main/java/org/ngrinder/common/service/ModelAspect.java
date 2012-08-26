/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.common.service;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.ngrinder.infra.spring.SpringContext;
import org.ngrinder.model.BaseModel;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * model aspect
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

	/**
	 * Save current user to modified date or created date. It's only workable when it's from servlet context.
	 * 
	 * @param joinPoint
	 *            joint point
	 */
	@Before(EXECUTION_SAVE)
	public void beforeSave(JoinPoint joinPoint) {
		for (Object object : joinPoint.getArgs()) {
			// If the object is base model and it's on request of servlet
			// It's not executed on Task scheduling.
			SpringContext springContext = getSpringContext();
			if (object instanceof BaseModel
					&& (springContext.isServletRequestContext() || springContext.isUnitTestContext())) {
				BaseModel<?> model = (BaseModel<?>) object;
				Date lastModifiedDate = new Date();
				model.setLastModifiedDate(lastModifiedDate);
				User currentUser = userContext.getCurrentUser();
				model.setLastModifiedUser(currentUser);
				if (!model.exist() || model.getCreatedUser() == null) {
					model.setCreatedDate(lastModifiedDate);
					model.setCreatedUser(currentUser);
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
