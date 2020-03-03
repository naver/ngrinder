package net.grinder.engine.communication;

import net.grinder.communication.Message;

import java.util.Map;
import java.util.Set;

/**
 * Message used to distribute a file md5 checksum the console to the agent processes.
 *
 * @since 3.5.1
 */
public class Md5Message implements Message {
	private final Set<String> md5;

	public Md5Message(Set<String> md5) {
		this.md5 = md5;
	}

	public Set<String> getMd5() {
		return md5;
	}
}
