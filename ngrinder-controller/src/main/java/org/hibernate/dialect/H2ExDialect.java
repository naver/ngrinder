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
package org.hibernate.dialect;

import java.sql.Types;

/**
 * Customized H2 dialect. it's necessary to be added because the original H2
 * dialect treats all float into float. So.. the Hibernate validation is failed
 * with that version.
 * 
 * @since 3.0
 */
public class H2ExDialect extends H2Dialect {
	/**
	 * Constructor.
	 */
	public H2ExDialect() {
		super();
		registerColumnType(Types.FLOAT, "double");
		registerColumnType(Types.BOOLEAN, "char(1)");
	}
}
