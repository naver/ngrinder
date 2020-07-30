package net.grinder.engine.communication;

import net.grinder.communication.Message;

import java.util.Set;

/**
 * Message used to distribute a file's digest.
 *
 * @since 3.5.0
 */
public class DistFilesDigestMessage implements Message {
	private final Set<String> distFilesDigest;

	public DistFilesDigestMessage(Set<String> distFilesDigest) {
		this.distFilesDigest = distFilesDigest;
	}

	public Set<String> getDistFilesDigest() {
		return distFilesDigest;
	}
}
