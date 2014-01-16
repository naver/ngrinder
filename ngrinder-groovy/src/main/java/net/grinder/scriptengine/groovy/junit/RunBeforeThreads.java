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

import net.grinder.script.Grinder;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread;
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

		doRampUp();
		for (FrameworkMethod before : fBefores) {
			before.invokeExplosively(testObject);
		}
		fNext.evaluate();
	}

	protected void doRampUp() {
		int rampUpInterval = 0;
		int rampUpStep = 1;
		int rampUpInitialThread = 0;
		int rampUpInitialSleep = 0;
		for (FrameworkMethod before : fBefores) {
			final BeforeThread annotation = before.getAnnotation(BeforeThread.class);
			if (annotation != null) {
				rampUpInterval = Math.max(annotation.interval(), rampUpInterval);
				rampUpStep = Math.max(annotation.step(), rampUpStep);
				rampUpInitialThread = Math.max(annotation.initialThread(), rampUpInitialThread);
				rampUpInitialSleep = Math.max(annotation.initialSleep(), rampUpInitialSleep);
			}
		}

		doRampup(rampUpInterval, rampUpStep, rampUpInitialThread, rampUpInitialSleep);
	}

	private void doRampup(int rampUpInterval, int rampUpStep, int rampUpInitialThread, int rampUpInitialSleep) {
		int threadNumber = 0;
		if (Grinder.grinder != null) {
			threadNumber = Math.max(Grinder.grinder.getThreadNumber(), 0);
		}
		try {
			final int waitingTime = getWaitingTime(rampUpInterval, rampUpStep, rampUpInitialThread, rampUpInitialSleep, threadNumber);
			if (waitingTime != 0) {
				if (Grinder.grinder != null) {
					Grinder.grinder.getLogger().info("Sleep {}ms for thread ramp-up ", waitingTime);
				}
				Thread.sleep(waitingTime);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while waiting for " +
					rampUpInterval + "(ms) for ramp up\n" +
					"thread number : " + threadNumber);
		}
	}

	public int getWaitingTime(int rampUpInterval, int rampUpStep,
	                          int rampUpInitialThread, int rampUpInitialSleep,
	                          int threadNumber) {
		// 100 2 1 0 3   ==> 100
		if (threadNumber < rampUpInitialThread) {
			return 0;
		}
		int remained = (threadNumber - rampUpInitialThread);
		int threadStep = (remained / rampUpStep) + 1;
		return Math.max(rampUpInitialSleep + (threadStep * rampUpInterval), 0);
	}

}
