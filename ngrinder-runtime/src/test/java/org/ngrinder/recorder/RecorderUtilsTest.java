package org.ngrinder.recorder;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

/**
 * @author Gisoo Gwon
 */
public class RecorderUtilsTest {
	
	private String requestTest = 
			"{\"BeforeThread\": {}, " +
			"\"Test\": {\"REQ_123\":{\"url\":\"http://domain.com\"}}, " + 
			"\"AfterThread\": {}}";
	
	@Test
	public void parseToJson() throws Exception {
		JSONObject requestJson = RecorderUtils.parseRequestToJson(requestTest);
		
		assertThat(requestJson, is(notNullValue()));
		assertThat(requestJson.getJSONObject("BeforeThread").length(), is(0));
		assertThat(requestJson.getJSONObject("Test").length(), is(1));
		assertThat(requestJson.getJSONObject("AfterThread").length(), is(0));
		
		JSONObject req_123 = requestJson.getJSONObject("Test").getJSONObject("REQ_123");
		assertThat(req_123.length(), is(1));
		assertThat(req_123.getString("url"), is("http://domain.com"));
	}
	
	@Test
	public void testExtractDomain() {
		String domain = RecorderUtils.extractDomain("http://domain.com:123/abc?a=b");
		assertThat(domain, is("domain.com"));
	}

}
