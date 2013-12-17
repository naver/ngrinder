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

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Property Wrapper Test
 */
public class PropertiesWrapperTest {

	@Test
	public void testPropertiesWrapper() {
		Properties prop = new Properties();
		prop.put("key1", "1");
		prop.put("key2", "value2");
		PropertiesWrapper propWrapper = new PropertiesWrapper(prop, PropertiesKeyMapper.create("agent-properties.map"));

		propWrapper.addProperty("key3", "3");
		propWrapper.addProperty("key4", "value4");

		int value1 = propWrapper.getPropertyInt("key1");
		assertThat(value1, is(1));
		int value3 = propWrapper.getPropertyInt("key3");
		assertThat(value3, is(3));
		try {
			int noValue = propWrapper.getPropertyInt("NoValueKey");
			fail("should cause IllegalArgumentException");
		} catch (IllegalArgumentException e) {

		}
		String value2 = propWrapper.getProperty("key2", "null");
		assertThat(value2, is("value2"));
		String value4 = propWrapper.getProperty("key4", "null");
		assertThat(value4, is("value4"));
		String nullValueStr = propWrapper.getProperty("NoValueKey", "null");
		assertThat(nullValueStr, is("null"));

		String newValue4 = propWrapper.getProperty("key4");
		assertThat(newValue4, is("value4"));
		nullValueStr = propWrapper.getProperty("NoValueKey", "null");
		assertThat(nullValueStr, is("null"));
		prop.put("BoolKey", "true");
		boolean boolVal = propWrapper.getPropertyBoolean("BoolKey");
		assertThat(boolVal, is(true));
		propWrapper.addProperty("hello_world", "wow");
		assertThat(propWrapper.getProperty("hello_world"), is("wow"));

	}
}
