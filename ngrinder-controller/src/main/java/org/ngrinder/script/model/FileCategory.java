package org.ngrinder.script.model;

public enum FileCategory {
	SCRIPT(true), DATA(true), LIBRARY(false), ETC(false);
	private final boolean isEditable;

	private FileCategory(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public boolean isEditable() {
		return isEditable;
	}

}
