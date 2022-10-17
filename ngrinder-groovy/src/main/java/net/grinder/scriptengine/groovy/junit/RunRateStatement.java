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

import org.junit.runners.model.Statement;

/**
 * <code>RunRateStatement</code> is a custom JUnit 4.5+ {@link Statement} which adds support for
 * {@link net.grinder.scriptengine.groovy.junit.annotation.RunRate} annotation by deciding if the
 * test is runnable for the current run.
 *
 * @see #evaluate()
 * @author JunHo Yoon
 * @since 3.2
 */
public class RunRateStatement extends Statement {

	private final Statement statement;
	private final float interval;
	private final float percent;

	/**
	 * Constructor.
	 *
	 * @param statement	statement to be repeated
	 * @param runRate	the percent of run
	 */
	public RunRateStatement(Statement statement, int runRate) {
		this.statement = statement;
		runRate = Math.max(Math.min(runRate, 100), 0);
		this.interval = 100f / runRate;
		this.percent = (runRate / 100f) + 0.000000012f;

	}

	@Override
	public void evaluate() throws Throwable {
		if (checkRun(Grinder.grinder.getRunNumber())) {
			statement.evaluate();
		}
	}

	private boolean checkRun(int i) {
		float f = i / interval;
		return (((int) f) != (int) (f + percent));
	}
}
