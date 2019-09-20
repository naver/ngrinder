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
package org.ngrinder.monitor.share.domain;

import javax.management.openmbean.CompositeData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Abstract class for monitor info.
 * 
 * @since 2.0
 */
@Getter
@Setter
@ToString
public abstract class MonitorInfo {

	private long collectTime;

	/**
	 * get monitor data from CompositeData.
	 * 
	 * @param cd
	 *            is CompositeData got from remote JMX server
	 */
	public abstract void parse(CompositeData cd);

	protected static Object getObject(CompositeData cd, String itemName) {
		return cd.get(itemName);
	}

	protected static String getString(CompositeData cd, String itemName) {
		return (String) getObject(cd, itemName);
	}

	protected static long getLong(CompositeData cd, String itemName) {
		return (Long) getObject(cd, itemName);
	}

	protected static int getInt(CompositeData cd, String itemName) {
		return (Integer) getObject(cd, itemName);
	}

	protected static float getFloat(CompositeData cd, String itemName) {
		return (Float) getObject(cd, itemName);
	}

	protected static boolean containsKey(CompositeData cd, String itemName) {
		return cd.containsKey(itemName);
	}

}
