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
package net.grinder.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.StringTokenizer;

/**
 * Comparable Version number.
 *
 * @author JunHo Yoon
 * @since 3.1.1
 */
public class VersionNumber implements Comparable<VersionNumber> {
	private final int[] digits;

	/**
	 * Parses a string like "1.0.2" into the version number.
	 *
	 * @param num parameter string
	 */
	public VersionNumber(String num) {
		StringTokenizer tokens = new StringTokenizer(num, ".-_");
		this.digits = new int[tokens.countTokens()];
		if (this.digits.length < 2) {
			throw new IllegalArgumentException("Failed to parse " + num + " as version number");
		}

		int i = 0;
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().toLowerCase();
			if (token.equals("*")) {
				this.digits[i++] = 1000;
			} else if (StringUtils.startsWithIgnoreCase(token, "snapshot")) {
				this.digits[i - 1]--;
				//noinspection UnusedAssignment
				this.digits[i++] = 1000;
				break;
			} else {
				if (NumberUtils.isNumber(token)) {
					this.digits[i++] = Integer.parseInt(token);
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < this.digits.length; i++) {
			if (i != 0) {
				buf.append('.');
			}
			buf.append(Integer.toString(this.digits[i]));
		}
		return buf.toString();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof VersionNumber && compareTo((VersionNumber) o) == 0;
	}

	@Override
	public int hashCode() {
		int x = 0;
		for (int i : this.digits) {
			x = x << 1 | i;
		}
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VersionNumber rhs) {
		for (int i = 0; ; i++) {
			if (i == this.digits.length && i == rhs.digits.length) {
				return 0; // equals
			}
			if (i == this.digits.length) {
				return -1; // rhs is larger
			}
			if (i == rhs.digits.length) {
				return 1;
			}

			int r = this.digits[i] - rhs.digits[i];
			if (r != 0) {
				return r;
			}
		}
	}
}
