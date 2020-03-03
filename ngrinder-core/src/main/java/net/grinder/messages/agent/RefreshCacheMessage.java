package net.grinder.messages.agent;

import net.grinder.communication.Message;

import java.util.Set;

/**
 * Message that refresh cache directory.
 *
 * @since 3.5.1
 * */
public final class RefreshCacheMessage implements Message {
	private Set<String> md5;

	public RefreshCacheMessage(Set<String> md5) {
		this.md5 = md5;
	}

	public Set<String> getMd5() {
		return md5;
	}
}

