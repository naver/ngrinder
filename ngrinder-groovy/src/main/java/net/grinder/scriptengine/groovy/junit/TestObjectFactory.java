package net.grinder.scriptengine.groovy.junit;

import java.util.HashMap;
import java.util.Map;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.model.TestClass;

abstract class TestObjectFactory {
	private Map<TestClass, Object> testObjectMap = new HashMap<TestClass, Object>();

	public TestObjectFactory() {
	}

	public Object getTestObject() {
		Object testObject = testObjectMap.get(getTestClass());
		if (testObject == null) {
			try {
				testObject = new ReflectiveCallable() {
					@Override
					protected Object runReflectiveCall() throws Throwable {
						return createTest();
					}
				}.run();
				testObjectMap.put(getTestClass(), testObject);
			} catch (Throwable e) {
				return new Fail(e);
			}
		}
		return testObject;
	}

	public abstract TestClass getTestClass();

	public abstract Object createTest() throws Exception;
}
