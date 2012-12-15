/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.dns;

/**
 * Static utilities used to convert IP Addresses between the various formats used internally by the
 * java.net.InetAddress class and associated classes.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class DnsUtil {
	// CHECKSTYLE:OFF
	public static final int INADDRSZ = 4;

	private DnsUtil() {
		// na
	}

	/**
	 * Check the string is empty.
	 * 
	 * @param str
	 *            string
	 * @return true if empty
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Check the string is not empty.
	 * 
	 * @param str
	 *            string
	 * @return true if not empty
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * Converts the internal integer representation of an IPv4 into a binary address.
	 * 
	 * @param src
	 *            integer representation of the IPv4 address
	 * @return a byte array representing an IPv4 numeric address
	 */
	public static byte[] intToNumericFormat(int src) {
		byte[] addr = new byte[INADDRSZ];

		addr[0] = (byte) ((src >>> 24) & 0xFF);
		addr[1] = (byte) ((src >>> 16) & 0xFF);
		addr[2] = (byte) ((src >>> 8) & 0xFF);
		addr[3] = (byte) (src & 0xFF);

		return addr;
	}

	/**
	 * Converts IPv4 binary address a single integer representation as used internally by
	 * Inet4Address.
	 * 
	 * @param addr
	 *            a byte array representing an IPv4 numeric address
	 * @return an integer representation of the IPv4 address
	 */
	public static int numericToIntFormat(byte[] addr) {
		int address = -1;

		if (addr.length == INADDRSZ) {
			address = addr[3] & 0xFF;
			address |= ((addr[2] << 8) & 0xFF00);
			address |= ((addr[1] << 16) & 0xFF0000);
			address |= ((addr[0] << 24) & 0xFF000000);
		}

		return address;
	}

	/**
	 * Converts IPv4 binary address into a string suitable for presentation.
	 * 
	 * @param src
	 *            a byte array representing an IPv4 numeric address
	 * @return a String representing the IPv4 address in textual representation format
	 */
	public static String numericToTextFormat(byte[] src) {
		return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
	}

	/**
	 * Converts IPv4 address in its textual presentation form into its numeric binary form.
	 * 
	 * @param src
	 *            a String representing an IPv4 address in standard format
	 * @return a byte array representing the IPv4 numeric address
	 */
	public static byte[] textToNumericFormat(String src) {
		if (src == null || src.length() == 0) {
			return null;
		}
		int octets;
		char ch;
		byte[] dst = new byte[INADDRSZ];
		char[] srcb = src.toCharArray();
		boolean sawDigit = false;

		octets = 0;
		int i = 0;
		int cur = 0;
		while (i < srcb.length) {
			ch = srcb[i++];
			if (Character.isDigit(ch)) {
				int sum = dst[cur] * 10 + (Character.digit(ch, 10) & 0xff);

				if (sum > 255) {
					return null;
				}
				dst[cur] = (byte) (sum & 0xff);
				if (!sawDigit) {
					if (++octets > INADDRSZ) {
						return null;
					}
					sawDigit = true;
				}
			} else if (ch == '.' && sawDigit) {
				if (octets == INADDRSZ) {
					return null;
				}
				cur++;
				dst[cur] = 0;
				sawDigit = false;
			} else {
				return null;
			}
		}

		if (octets < INADDRSZ) {
			return null;
		}
		return dst;
	}

}
