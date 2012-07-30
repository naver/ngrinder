package org.ngrinder.common.service;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;

import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.ngrinder.infra.spring.SpringContextUtils;
import org.ngrinder.model.BaseModel;

public class ModelAspectTest {
	@Test
	public void testModelAspect() {
		assertThat(SpringContextUtils.isServletRequestContext(), notNullValue());
		ModelAspect aspect = new ModelAspect();

		JoinPoint joinPoint = mock(JoinPoint.class);
		@SuppressWarnings("unchecked")
		BaseModel<Object> baseModel = mock(BaseModel.class);
		when(joinPoint.getArgs()).thenReturn(new Object[] { baseModel });
		aspect.beforeSave(joinPoint);
		verify(baseModel, times(0)).setCreatedDate(any(Date.class));
	}
}
