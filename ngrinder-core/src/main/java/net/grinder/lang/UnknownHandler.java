package net.grinder.lang;

import net.grinder.util.AbstractGrinderClassPathProcessor;

public class UnknownHandler extends AbstractLanguageHandler {

	public UnknownHandler() {
		super("unknown", "unknown", "unknown");
	}

	@Override
	public AbstractGrinderClassPathProcessor getClassPathProcesssor() {
		return null;
	}

	@Override
	public String checkSyntaxErrors(String script) {
		return null;
	}

}
