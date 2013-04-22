package net.grinder.scriptengine.groovy.junit;

import org.junit.runners.model.Statement;

public class RepetitionStatment extends Statement {

	private final int repetition;
	private final Statement statement;

	public RepetitionStatment(Statement statement, int repetition) {
		this.statement = statement;
		this.repetition = repetition;
	}

	@Override
	public void evaluate() throws Throwable {
		for (int i = 0; i < repetition; i++) {
			statement.evaluate();
		}
	}
}
