package org.ngrinder.common.service;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.ngrinder.model.BaseModel;
import org.ngrinder.user.util.UserUtil;
import org.springframework.stereotype.Service;



/**
 * model aspect
 * 
 * @author Liu Zhifei
 * @date 2012-6-19
 */
@Aspect
@Service
public class ModelAspect {

	public static final String EXECUTION_SAVE = "execution(* org.ngrinder.**.service.*Service.save*(..))";

	@Before(EXECUTION_SAVE)
	public void beforeSave(JoinPoint joinPoint) {
		for (Object object : joinPoint.getArgs()) {
			if (object instanceof BaseModel) {
				BaseModel model = (BaseModel) object;
				if (0 != model.getId()) {
					model.setLastModifiedDate(new Date());
					model.setLastModifiedUser(UserUtil.getCurrentUser().getName());
				} else {
					model.setCreateDate(new Date());
					model.setCreateUser(UserUtil.getCurrentUser().getName());
				}
			}
		}
	}

}
