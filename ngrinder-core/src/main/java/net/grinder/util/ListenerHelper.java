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

/**
 * Listener Helper to shorten the {@link ListenerSupport} creation code.
 *
 * @author JunHo Yoon
 * @since 3.0.2
 */
public abstract class ListenerHelper {
	/**
	 * Create a listener instance.
	 *
	 * @param <T>	listener type.
	 * @return created listener
	 */
	public static <T> ListenerSupport<T> create() {
		return new ListenerSupport<>();
	}
}
