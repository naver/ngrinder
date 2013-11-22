package org.ngrinder.common.util;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Convenient get checksum value utilities.
 *
 * @author Matt
 * @since 3.3
 */
public class CRC32ChecksumUtils {

	/**
	 * Generate CRC32 Checksum For Byte Array.
	 *
	 * @param bytes byte array
	 * @return checksum CRC32 checksum value
	 * @since 3.3
	 */
	public static long getCRC32Checksum(byte[] bytes) {
		Checksum checksum = new CRC32();
		checksum.update(bytes, 0, bytes.length);
		return checksum.getValue();
	}
}
