/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.common.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ngrinder.common.constant.NGrinderConstants;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class JSONUtilTest implements NGrinderConstants {

	JsonParser parser = new JsonParser();

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnSuccess(java.lang.String)}.
	 */
	
	@Test
	public void testReturnSuccessString() {
		String rtnStr = JSONUtil.returnSuccess("return message");
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(json.get(JSON_SUCCESS).getAsBoolean());
		assertTrue(json.get(JSON_MESSAGE).getAsString().contains("return message"));
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnError(java.lang.String)}.
	 */
	@Test
	public void testReturnErrorString() {
		String rtnStr = JSONUtil.returnError("return message");
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(!json.get(JSON_SUCCESS).getAsBoolean());
		assertTrue(json.get(JSON_MESSAGE).getAsString().contains("return message"));
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnSuccess()}.
	 */
	@Test
	public void testReturnSuccess() {
		String rtnStr = JSONUtil.returnSuccess();
		JsonObject json = (JsonObject)parser.parse(rtnStr);
		assertTrue(json.get(JSON_SUCCESS).getAsBoolean());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#returnError()}.
	 */
	@Test
	public void testReturnError() {
		String rtnStr = JSONUtil.returnError();
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
		JSONUtil.toJson(intList);
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.JSONUtil#toJson(java.util.Map)}.
	 */
	@Test
	public void testToJsonMapOfStringObject() {
		Map<String, Object> intMap = new HashMap<String, Object>();
		intMap.put("kay1", 1);
		intMap.put("kay2", 2);
		JSONUtil.toJson(intMap);
	}

}
