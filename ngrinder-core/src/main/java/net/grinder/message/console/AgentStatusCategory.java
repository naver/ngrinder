package net.grinder.message.console;

/**
 * Agent Status. This enum describes all necessary step and status which {@link org.ngrinder.model.AgentInfo}
 * can be in.
 *
 * @author Matt
 * @since 3.3
 */
public enum AgentStatusCategory {

	/**
	 * Processing.
	 */
	PROGRESSING("green_anime.gif", true),
	/**
	 * Ready.
	 */
	READY("green.png", true),

	/**
	 * Unknown state.
	 */
	INACTIVE("grey.png", false),
	/**
	 * Stopped by error .
	 */
	ERROR("red.png", false);
	private final String iconName;
	private boolean active;

	AgentStatusCategory(String iconName, boolean active) {
		this.iconName = iconName;
		this.active = active;
	}

	public String getIconName() {
		return iconName;
	}

	public boolean isActive() {
		return active;
	}
}
