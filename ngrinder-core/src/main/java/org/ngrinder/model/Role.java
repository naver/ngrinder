package org.ngrinder.model;

/** Role of the User */
public enum Role {
	/** User */
	USER("U"),
	/** Admin */
	ADMIN("A"),
	/** Super User */
	SUPER_USER("S");

	private final String shortName;

	Role(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}
}