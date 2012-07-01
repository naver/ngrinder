package org.ngrinder.script.model;

public enum ScriptType {
	PYTHON_SCRIPT("Python Script"), JAVA_SCRIPT("JavaScript"), XML("xml"), TXT("txt"), PROPERTIES("properties");

	private String description;

	ScriptType(String description) {
		this.description = description;
	}

	public String toString() {
		return description;
	}

}
