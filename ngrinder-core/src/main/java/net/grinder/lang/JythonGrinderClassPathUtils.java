package net.grinder.lang;

import net.grinder.util.GrinderClassPathProcessor;

public class JythonGrinderClassPathUtils extends GrinderClassPathProcessor {

	public JythonGrinderClassPathUtils() {
		super();
	}

	@Override
	protected void initMore() {
		USEFUL_JAR_LIST.add("jython-2.5");
		USEFUL_JAR_LIST.add("jython-standalone-2.5");
	}

}
