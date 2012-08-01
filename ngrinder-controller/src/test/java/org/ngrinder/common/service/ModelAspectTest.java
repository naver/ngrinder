package org.ngrinder.common.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;

import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.spring.SpringContext;
import org.ngrinder.model.BaseModel;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelAspectTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private SpringContext springContext;

	@Autowired
	private ModelAspect modelAspect;

	@Test
	public void testModelAspect() {
		assertThat(springContext.isServletRequestContext(), is(false));
		JoinPoint joinPoint = mock(JoinPoint.class);
		@SuppressWarnings("unchecked")
		BaseModel<Object> baseModel = mock(BaseModel.class);
		when(joinPoint.getArgs()).thenReturn(new Object[] { baseModel });
		modelAspect.beforeSave(joinPoint);
		verify(baseModel, times(0)).setCreatedDate(any(Date.class));
	}

	@Test
	public void testModelAspectOnServletContext() {
		springContext = spy(springContext);
		when(springContext.isServletRequestContext()).thenReturn(true);
		JoinPoint joinPoint = mock(JoinPoint.class);
		@SuppressWarnings("unchecked")
		BaseModel<Object> baseModel = mock(BaseModel.class);
		when(baseModel.exist()).thenReturn(true);
		when(joinPoint.getArgs()).thenReturn(new Object[] { baseModel });
		modelAspect.setSpringContext(springContext);
		modelAspect.beforeSave(joinPoint);
		verify(baseModel, times(1)).setLastModifiedDate(any(Date.class));
	}
}
