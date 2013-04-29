package org.ngrinder.script.handler;

import groovy.lang.GroovyClassLoader;

import org.springframework.stereotype.Component;

@Component
public class GroovyScriptHandler extends ScriptHandler {

	public GroovyScriptHandler() {
		super("groovy", "Groovy Single Script", "groovy");
	}

	public GroovyScriptHandler(String title) {
		super("groovy", title, "groovy");
	}

	@Override
	protected Integer order() {
		return 300;
	}

	@Override
	public String checkSyntaxErrors(String script) {
		GroovyClassLoader loader = new GroovyClassLoader();
		try {
			loader.parseClass(script);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
}
