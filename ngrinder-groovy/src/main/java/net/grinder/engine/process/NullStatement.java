package net.grinder.engine.process;

import org.junit.runners.model.Statement;

public class NullStatement extends Statement {
	private static NullStatement instance = new NullStatement();

	public NullStatement() {

	}

	@Override
	public void evaluate() throws Throwable {
		// Do Nothing
	}

	public static Statement getInstance() {
		return instance;
	}
}
