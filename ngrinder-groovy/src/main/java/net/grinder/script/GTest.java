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
package net.grinder.script;

/**
 * {@link Test} extension to be distinguishable from {@link org.junit.Test} in
 * JUnit4 tests.
 * 
 * <code>
 * request = new HTTPRequest();
 * GTest(1, "Test").record(request);
 * </code>
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class GTest extends Test {

	/** UID */
	private static final long serialVersionUID = 8370116882992463352L;

	/**
	 * Constructor.
	 * 
	 * @param number
	 *            the test number
	 * @param description
	 *            test description
	 */
	public GTest(int number, String description) {
		super(number, description);
	}

}
