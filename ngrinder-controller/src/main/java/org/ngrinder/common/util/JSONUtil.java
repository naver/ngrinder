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

import org.json.simple.JSONObject;
import org.ngrinder.common.NGrinderConstants;



@SuppressWarnings("unchecked")
public class JSONUtil {
	
	private static String successJson;
	private static String errorJson;
	
	static {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, true);
		successJson = rtnJson.toJSONString();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, false);
		errorJson = rtnJson.toJSONString();
	}
	
	public static String returnSuccess(String message) {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, true);
		rtnJson.put(NGrinderConstants.JSON_MESSAGE, message);
		return rtnJson.toJSONString();
	}
	
	public static String returnError(String message) {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, false);
		rtnJson.put(NGrinderConstants.JSON_MESSAGE, message);
		return rtnJson.toJSONString();
	}

	public static String returnSuccess() {
		return successJson;
	}

	public static String returnError() {
		return errorJson;
	}

}
