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
package org.ngrinder.script.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HAR {
	private Log log;
	public Log getLog() {
		return log;
	}
	public void setLog(Log log) {
		this.log = log;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Log {
		private List<HAREntry> entries;

		public List<HAREntry> getEntries() {
			return entries;
		}

		public void setEntries(List<HAREntry> entries) {
			this.entries = entries;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class HAREntry {
		private Request request;
		private Response response;

		public Request getRequest() {
			return request;
		}

		public void setRequest(Request request) {
			this.request = request;
		}

		public Response getResponse() {
			return response;
		}

		public void setResponse(Response response) {
			this.response = response;
		}

		public HAREntry() {
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	public static class Request {
		private String method;
		private String url;
		private List<Header> headers;
		private postData postData;

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public List<Header> getHeaders() {
			return headers;
		}

		public void setHeaders(List<Header> headers) {
			this.headers = headers;
		}

		public postData getPostData() {
			return postData;
		}

		public void setPostData(postData postData) {
			this.postData = postData;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response {
		private String status;
		private List<Header> headers;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public List<Header> getHeaders() {
			return headers;
		}

		public void setHeaders(List<Header> headers) {
			this.headers = headers;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Header {
		private String name;
		private String value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class postData {
		private List<param> params;

		public List<param> getParams() {
			return params;
		}

		public void setParams(List<param> params) {
			this.params = params;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class param {
		private String name;
		private String value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}
