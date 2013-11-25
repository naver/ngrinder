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
	PROGRESSING("green_anime.gif"),
	/**
	 * Ready.
	 */
	READY("green.png"),

	/**
	 * Unknown state.
	 */
	UNKNOWN("grey.png"),
	/**
	 * Stopped by error .
	 */
	ERROR("red.png");
	private final String iconName;

	AgentStatusCategory(String iconName) {
		this.iconName = iconName;
	}

	public String getIconName() {
		return iconName;
	}
}
