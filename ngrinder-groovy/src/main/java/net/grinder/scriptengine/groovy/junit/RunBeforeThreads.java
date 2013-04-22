package net.grinder.scriptengine.groovy.junit;

import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

class RunBeforeThreads extends Statement {
	private final Statement fNext;

	private final TestObjectFactory fFactory;

	private final List<FrameworkMethod> fBefores;

	private final PerThreadStatement fDefault;

	public RunBeforeThreads(Statement next, List<FrameworkMethod> befores, TestObjectFactory factory,
					PerThreadStatement defaultPerThreadStat) {
		fNext = next;
		fBefores = befores;
		fFactory = factory;
		fDefault = defaultPerThreadStat;
	}

	@Override
	public void evaluate() throws Throwable {
		if (fDefault != null) {
			fDefault.before();
		}
		Object testObject = fFactory.getTestObject();
		for (FrameworkMethod before : fBefores)
			before.invokeExplosively(testObject);
		fNext.evaluate();
	}
}
