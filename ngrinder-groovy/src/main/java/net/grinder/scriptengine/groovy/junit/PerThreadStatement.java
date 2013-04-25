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
package net.grinder.scriptengine.groovy.junit;

/**
 * {@link PerThreadStatement} is the abstract class which let define statements which is invoked
 * before and after the nesting statement.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
abstract class PerThreadStatement {

	/**
	 * method to be executed just after thread initiation.
	 */
	abstract void before();

	/**
	 * method to be executed just before thread termination.
	 */
	abstract void after();
}
