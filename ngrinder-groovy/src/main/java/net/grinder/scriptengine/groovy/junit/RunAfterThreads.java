package net.grinder.scriptengine.groovy.junit;

import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

class RunAfterThreads extends Statement {
	private final Statement fNext;

	private final TestObjectFactory fFactory;

	private final List<FrameworkMethod> fBefores;

	private final PerThreadStatement fDefault;

	public RunAfterThreads(Statement next, List<FrameworkMethod> befores, TestObjectFactory factory,
					PerThreadStatement defaultStatement) {
		fNext = next;
		fBefores = befores;
		fFactory = factory;
		fDefault = defaultStatement;
	}

	@Override
	public void evaluate() throws Throwable {
		fNext.evaluate();
		Object testObject = fFactory.getTestObject();
		for (FrameworkMethod before : fBefores)
			before.invokeExplosively(testObject);
		if (fDefault != null) {
			fDefault.after();
		}
	}
}
