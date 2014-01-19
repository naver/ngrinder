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
package net.grinder.scriptengine.groovy.junit;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * Statement which runs just after thread initiation.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
class RunBeforeThreads extends Statement {
	private final Statement fNext;

	private final TestObjectFactory fFactory;

	@SuppressWarnings("SpellCheckingInspection")
	private final List<FrameworkMethod> fBefores;

	private final PerThreadStatement fFirstPerThreadStatement;

	/**
	 * Statement.
	 *
	 * @param next                    next
	 * @param befores                 method to be invoked in prior to thread termination.
	 * @param factory                 test object factory.
	 * @param firstPerThreadStatement first statement to be executed at the thread beginning.
	 */
	public RunBeforeThreads(Statement next, @SuppressWarnings("SpellCheckingInspection") List<FrameworkMethod> befores, TestObjectFactory factory,
	                        PerThreadStatement firstPerThreadStatement) {
		fNext = next;
		fBefores = befores;
		fFactory = factory;
		fFirstPerThreadStatement = firstPerThreadStatement;
	}

	@Override
	public void evaluate() throws Throwable {
		if (fFirstPerThreadStatement != null) {
			fFirstPerThreadStatement.before();
		}
		Object testObject = fFactory.getTestObject();
		for (FrameworkMethod before : fBefores) {
			before.invokeExplosively(testObject);
		}
		fNext.evaluate();
	}
}
