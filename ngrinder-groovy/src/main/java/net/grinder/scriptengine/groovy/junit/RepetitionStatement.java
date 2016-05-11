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

import net.grinder.engine.process.JUnitThreadContextUpdater;

import org.junit.runners.model.Statement;

/**
 * <code>RepetitionStatement</code> is a custom JUnit 4.5+ {@link Statement} which adds support for
 * {@link net.grinder.scriptengine.groovy.junit.annotation.Repeat} annotation by repeating the
 * test for the specified number of times.
 * 
 * @see #evaluate()
 * @author JunHo Yoon
 * @since 3.2
 */
public class RepetitionStatement extends Statement {

	private final int repetition;
	private final Statement statement;
	private JUnitThreadContextUpdater threadContextUpdater;

	/**
	 * Constructor.
	 * 
	 * @param statement		statement to be repeated
	 * @param repetition	the repetition count
	 */
	@SuppressWarnings("UnusedDeclaration")
	public RepetitionStatement(Statement statement, int repetition) {
		this.statement = statement;
		this.repetition = Math.max(repetition, 1);
	}

	/**
	 * Constructor.
	 * 
	 * @param statement		statement to be repeated
	 * @param repetition	the repetition count
	 * @param threadContextUpdater	threadContextUpdater
	 * @since 3.2.1
	 */
	public RepetitionStatement(Statement statement, int repetition, JUnitThreadContextUpdater threadContextUpdater) {
		this.statement = statement;
		this.threadContextUpdater = threadContextUpdater;
		this.repetition = Math.max(repetition, 1);
	}

	@Override
	public void evaluate() throws Throwable {
		for (int i = 0; i < repetition; i++) {
			if (threadContextUpdater != null) {
				threadContextUpdater.setRunCount(i);
			}
			statement.evaluate();
		}
	}
}
