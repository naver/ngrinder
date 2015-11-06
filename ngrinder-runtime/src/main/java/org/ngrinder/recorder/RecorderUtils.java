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
package org.ngrinder.recorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.grinder.common.GrinderException;
import net.grinder.plugin.http.HTTPPluginControl;
import net.grinder.plugin.http.HTTPRequest;

import HTTPClient.Cookie;
import HTTPClient.CookieModule;
import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;

/**
 * Util class for nGrinder Recorder(Chrome extension)
 * @author Gisoo Gwon
 */
public class RecorderUtils {
	
	private static Pattern urlPattern = Pattern.compile("^[^:]+:\\/\\/([^\\/:]+)[:\\/]?.*$");
	private static Pattern fileTagPattern = Pattern.compile("<nGrinderRecorderFileName>([^<]+)<\\/nGrinderRecorderFileName>");

	/**
	 * @param requestText Json format text
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject parseRequestToJson(String requestText) throws JSONException {
		return new JSONObject(requestText);
	}
	
	public static HTTPResponse sendBy(HTTPRequest request, JSONObject requestJson) throws Exception {
		initRequest(request, requestJson);
		return send(request, requestJson);
	}
	
	private static void initRequest(HTTPRequest request, JSONObject requestJson) throws Exception {
		String domain = extractDomain(requestJson.getString("url"));
		setRequestHeader(request, requestJson, domain);
		setRequestBody(request, requestJson);
	}

	/**
	 * Extract domain uging regex.
	 * @param url
	 * @return
	 */
	static String extractDomain(String url) {
		Matcher m = urlPattern.matcher(url);
		if (m.find()) {
			return m.group(1); 
		}
		return null;
	}

	/**
	 * Set request header by requestJson.headers
	 * @param request	Target request object
	 * @param req_123	JSON of request info
	 * @param domain	Cookie domain
	 * @throws JSONException 
	 * @throws GrinderException 
	 */
	@SuppressWarnings("unchecked")
	private static void setRequestHeader(HTTPRequest request, JSONObject requestJson, String domain)
		throws Exception {
		List<NVPair> headers = new ArrayList<NVPair>();
		JSONObject headersJson = requestJson.getJSONObject("headers");
		Iterator<String> keys = headersJson.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.equals("Cookie") && requestJson.has("useCookie") && requestJson.getBoolean("useCookie")) {
				setCookie(headersJson.getString("Cookie"), domain);
			}
			headers.add(new NVPair(key, headersJson.getString(key)));
		}
		request.setHeaders(headers.toArray(new NVPair[0]));
	}

	/**
	 * Add cookie in {@link CookieModule}
	 * @param cookieHeader
	 * @param domain
	 * @throws GrinderException 
	 */
	private static void setCookie(String cookieHeader, String domain) throws GrinderException {
		for (String cookie : cookieHeader.split(";")) {
			String[] split = cookie.trim().split("=");
			if (split.length == 2) {
				CookieModule.addCookie(new Cookie(split[0], split[1], domain, "/", new Date(
					32503647599000L), false), HTTPPluginControl.getThreadHTTPClientContext());
			}
		}
	}

	private static void setRequestBody(HTTPRequest request, JSONObject requestJson) throws Exception {
		request.setData(null);
		request.setFormData(null);
		if (!requestJson.has("formData")) {
			return;
		}
		
		Object formData = requestJson.get("formData");
		if (formData instanceof String) {
			initStringBody(request, formData);
			return;
		}
		initPairDataBody(request, formData);
	}
	
	private static HTTPResponse send(HTTPRequest request, JSONObject requestJson) throws Exception {
		String method = requestJson.getString("method");
		String url = requestJson.getString("url");
		if (method.equals("POST")) {
			return request.POST(url);
		} else if (method.equals("PUT")) {
			return request.PUT(url);
		} else if (method.equals("GET")) {
			return request.GET(url);
		} else if (method.equals("HEAD")) {
			return request.HEAD(url);
		} else if (method.equals("DELETE")) {
			return request.DELETE(url);
		} else if (method.equals("OPTIONS")) {
			return request.OPTIONS(url);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void initPairDataBody(HTTPRequest request, Object formData) throws Exception {
		List<NVPair> datas = new ArrayList<NVPair>();
		JSONObject formDataJson = (JSONObject) formData;
		Iterator<String> keys = formDataJson.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONArray values = formDataJson.getJSONArray(key);
			for (int i = 0; i < values.length(); i++) {
				String value = values.getString(i);
				datas.add(new NVPair(key, value));
			}
		}
		request.setFormData(datas.toArray(new NVPair[0]));
	}

	private static void initStringBody(HTTPRequest request, Object formData) throws Exception {
		String body = (String) formData;
		String replacedBody = body;
		Matcher m = fileTagPattern.matcher(body);
		while (m.find()) {
			String filepath = m.group(1);
			File file = new File(filepath);
			String content = FileUtils.readFileToString(file, "UTF-8");
			replacedBody = replacedBody.replace(m.group(), content);
		}
		request.setData(replacedBody.getBytes("utf-8"));
	}
	
}
