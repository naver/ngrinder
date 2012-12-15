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
package org.ngrinder.common.util;

/**
 * Utility class for path manipulation.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public abstract class PathUtil {
	
	/**
	 * Remove prepending / on path.
	 * 
	 * @param path
	 *            path containning /
	 * @return / removed path
	 */
	public static String removePrependedSlash(String path) {
		if (path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}
	
	/**
	 * Remove prepending / on path.
	 * 
	 * @param path
	 *            path containning /
	 * @return / removed path
	 */
	public static String removeDuplicatedPrependedSlash(String path) {
		if (path.startsWith("//")) {
			return path.substring(1);
		}
		return path;
	}
}
