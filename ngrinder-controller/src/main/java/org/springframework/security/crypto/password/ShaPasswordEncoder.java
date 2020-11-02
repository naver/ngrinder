package org.springframework.security.crypto.password;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;

/**
 * Support password encryption based on SHA algorithm.
 *
 * @since 3.5.0
 */
public class ShaPasswordEncoder {
	private static final String PREFIX = "{";
	private static final String SUFFIX = "}";
	private final Digester digester;

	/**
	 * @param algorithm encryption algorithm
	 */
	public ShaPasswordEncoder(String algorithm) {
		if (!"SHA-256".equals(algorithm) && !"SHA-1".equals(algorithm)) {
			throw new IllegalArgumentException(algorithm + " is not supported algorithm");
		}
		this.digester = new Digester(algorithm, 1);
	}

	public String encode(String rawSalt, CharSequence rawPassword) {
		return digest(makeSalt(rawSalt), rawPassword);
	}

	public boolean matches(String rawSalt, CharSequence rawPassword, String encodedPassword) {
		String rawPasswordEncoded = digest(makeSalt(rawSalt), rawPassword);
		return PasswordEncoderUtils.equals(encodedPassword, rawPasswordEncoded);
	}

	private String digest(String salt, CharSequence rawPassword) {
		String saltedPassword = rawPassword + salt;
		byte[] digest = this.digester.digest(Utf8.encode(saltedPassword));
		return encode(digest);
	}

	private String encode(byte[] digest) {
		return new String(Hex.encode(digest));
	}

	private String makeSalt(String rawSalt) {
		return PREFIX + rawSalt + SUFFIX;
	}
}
