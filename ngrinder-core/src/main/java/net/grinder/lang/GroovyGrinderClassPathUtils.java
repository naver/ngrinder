package net.grinder.lang;

import net.grinder.util.GrinderClassPathProcessor;

public class GroovyGrinderClassPathUtils extends GrinderClassPathProcessor {

	public GroovyGrinderClassPathUtils() {
		super();
	}

	@Override
	protected void initMore() {
		USEFUL_JAR_LIST.add("jython-2.5");
		USEFUL_JAR_LIST.add("jython-standalone-2.5");
		USEFUL_JAR_LIST.add("ngrinder-groovy");
		USEFUL_JAR_LIST.add("groovy");
	}

}
