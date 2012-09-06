package org.ngrinder.infra.init;

import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.infra.spring.SpringContext;

/**
 * Convenient class to determine spring context. It's mocked version which will be used in unit test
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@TestOnlyComponent
public class MockSpringContext extends SpringContext {
	/**
	 * Determine if this context is on unit test.
	 * 
	 * @see SpringContext
	 * @return always true.
	 */
	@Override
	public boolean isUnitTestContext() {
		return true;
	}
}
