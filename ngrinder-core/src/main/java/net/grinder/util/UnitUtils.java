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

import java.text.DecimalFormat;

/**
 * Unit Conversion Utility.
 * 
 * @author JunHo Yoon
 * @since 3.1.2
 */
public abstract class UnitUtils {

	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1024;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;
	/**
	 * The number of bytes in a gigabyte.
	 */
	public static final long ONE_GB = ONE_KB * ONE_MB;

	/**
	 * Get the display of the given byte size.
	 * 
	 * @param size	size
	 * @return display version of the byte size
	 */
	public static String byteCountToDisplaySize(long size) {
		String displaySize;
		DecimalFormat format = new DecimalFormat("###0.0");
		if (size / ONE_GB > 0) {
			displaySize = format.format((double) size / ONE_GB) + "GB";
		} else if (size / ONE_MB > 0) {
			displaySize = format.format((double) size / ONE_MB) + "MB";
		} else if (size / ONE_KB > 0) {
			displaySize = format.format((double) size / ONE_KB) + "KB";
		} else {
			displaySize = String.valueOf(size) + "B";
		}
		return displaySize;
	}
}
