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
 * Convenient utilities for field access.
 */
public abstract class AccessUtils {
	public static int getSafe(Integer value) {
		return (value == null) ? 0 : value;
	}

	public static <T> T getSafe(T value, T defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	public static long getSafe(Long value) {
		return (value == null) ? 0 : value;
	}

	public static boolean getSafe(Boolean value) {
		return (value == null) ? false : value;
	}

	public static boolean getSafe(Boolean value, boolean b) {
		return (value == null) ? b : value;
	}
}
