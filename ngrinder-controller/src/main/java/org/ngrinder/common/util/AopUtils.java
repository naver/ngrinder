package org.ngrinder.common.util;


import org.springframework.aop.framework.AopContext;

import static org.ngrinder.common.util.TypeConvertUtils.cast;

public class AopUtils {
	public static <T> T proxy(T current) {
		try {
			return cast(AopContext.currentProxy());
		} catch (IllegalStateException e) {
			return current;
		}
	}
}
