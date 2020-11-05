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
import org.junit.Assume;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.spring.SpringContext;
import org.ngrinder.model.BaseModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ModelAspectTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private SpringContext springContext;

	@Autowired
	private ModelAspect modelAspect;

	@Test
	public void testModelAspect() {
		Assume.assumeThat(springContext.isAuthenticationContext(), is(false));
		JoinPoint joinPoint = mock(JoinPoint.class);
		@SuppressWarnings("unchecked")
		BaseModel<Object> baseModel = mock(BaseModel.class);
		when(joinPoint.getArgs()).thenReturn(new Object[] { baseModel });
		modelAspect.beforeSave(joinPoint);
		verify(baseModel, times(1)).setCreatedAt(any(Instant.class));
	}

	@Test
	public void testModelAspectOnServletContext() {
		springContext = spy(springContext);
		when(springContext.isAuthenticationContext()).thenReturn(true);
		JoinPoint joinPoint = mock(JoinPoint.class);
		@SuppressWarnings("unchecked")
		BaseModel<Object> baseModel = mock(BaseModel.class);
		when(baseModel.exist()).thenReturn(true);
		when(joinPoint.getArgs()).thenReturn(new Object[] { baseModel });
		setField(modelAspect, "springContext", springContext);
		modelAspect.beforeSave(joinPoint);
		verify(baseModel, times(1)).setLastModifiedAt(any(Instant.class));
	}
}
