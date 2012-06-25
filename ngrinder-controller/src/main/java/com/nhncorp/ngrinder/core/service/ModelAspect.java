package com.nhncorp.ngrinder.core.service;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;

import com.nhncorp.ngrinder.core.model.BaseModel;

/**
 * model aspect
 * 
 * @author Liu Zhifei
 * @date 2012-6-19
 */
@Aspect
@Service
public class ModelAspect {

	public static final String EXECUTION_SAVE = "execution(* com.nhncorp.ngrinder.**.service.*Service.save*(..))";

	@Before(EXECUTION_SAVE)
	public void beforeSave(JoinPoint joinPoint) {
		for (Object object : joinPoint.getArgs()) {
			if (object instanceof BaseModel) {
				BaseModel model = (BaseModel) object;
				if (0 != model.getId()) {
					model.setLastModifiedDate(new Date());
					model.setLastModifiedUser("tmp_user2");
				} else {
					model.setCreateDate(new Date());
					model.setCreateUser("tmp_user1");
				}
			}
		}
	}
}
