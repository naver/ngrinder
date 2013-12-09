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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ngrinder.common.controller.BaseController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class JSONUtilTest extends BaseController {

	JsonParser parser = new JsonParser();

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnSuccess(java.lang.String)}.
	 */
	
	@Test
	public void testReturnSuccessString() {
		String rtnStr = returnSuccess("return message");
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(json.get(JSON_SUCCESS).getAsBoolean());
		assertTrue(json.get(JSON_MESSAGE).getAsString().contains("return message"));
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnError(java.lang.String)}.
	 */
	@Test
	public void testReturnErrorString() {
		String rtnStr = returnError("return message");
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(!json.get(JSON_SUCCESS).getAsBoolean());
		assertTrue(json.get(JSON_MESSAGE).getAsString().contains("return message"));
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnSuccess()}.
	 */
	@Test
	public void testReturnSuccess() {
		String rtnStr = returnSuccess();
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(json.get(JSON_SUCCESS).getAsBoolean());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnError()}.
	 */
	@Test
	public void testReturnError() {
		String rtnStr = returnError();
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(!json.get(JSON_SUCCESS).getAsBoolean());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#toJson(java.util.List)}.
	 */
	@Test
	public void testToJsonListOfQ() {
		List<Integer> intList = new ArrayList<Integer>();
		intList.add(1);
		intList.add(2);
		intList.add(3);
		toJson(intList);
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#toJson(java.util.Map)}.
	 */
	@Test
	public void testToJsonMapOfStringObject() {
		Map<String, Object> intMap = new HashMap<String, Object>();
		intMap.put("kay1", 1);
		intMap.put("kay2", 2);
		JsonObject json = (JsonObject)parser.parse(toJson(intMap));
		assertThat(json.get("kay1").getAsInt(), is(1));
		assertThat(json.get("kay2").getAsInt(), is(2));
	}

}
