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
abstract class DnsUtils {
	// CHECKSTYLE:OFF
	private static final int INADDRSZ = 4;

	private DnsUtils() {
		// na
	}

	/**
	 * Check the string is empty.
	 * 
	 * @param str	string
	 * @return true if empty
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	// swaps array elements i and j
	private static <T> void exch(T[] a, int i, int j) {
		T swap = a[i];
		a[i] = a[j];
		a[j] = swap;
	}

	// take as input an array of strings and rearrange them in random order
	// Fisher-Yates shuffle by Durstenfeld, O(n) complexity
	public static <T> T[] shuffle(T[] a) {
		int N = a.length;
		for (int i = 0; i < N; i++) {
			int r = i + (int) (Math.random() * (N-i));   // between i and N-1
			exch(a, i, r);
		}
		return a;
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
