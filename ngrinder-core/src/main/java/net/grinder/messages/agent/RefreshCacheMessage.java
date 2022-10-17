package net.grinder.messages.agent;

import net.grinder.communication.Message;

import java.util.Set;

/**
 * Message that refresh cache directory.
 *
 * @since 3.5.0
 * */
public final class RefreshCacheMessage implements Message {
	private final Set<String> disFilesDigest;

	public RefreshCacheMessage(Set<String> disFilesDigest) {
		this.disFilesDigest = disFilesDigest;
	}

	public Set<String> getDisFilesDigest() {
		return disFilesDigest;
	}
}

