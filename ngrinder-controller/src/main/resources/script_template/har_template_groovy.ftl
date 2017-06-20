import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import java.util.Date
import java.util.List
import java.util.ArrayList

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class TestRunner {

	public static GTest test
	public static HTTPRequest request
	public static NVPair[] headers = []

	@BeforeProcess
	public static void beforeProcess() {
		HTTPPluginControl.getConnectionDefaults().timeout = 6000
		test = new GTest(1, "HAR2Script")
		request = new HTTPRequest()
		grinder.logger.info("before process.");
	}

	@BeforeThread
	public void beforeThread() {
		test.record(this, "test")
		grinder.statistics.delayReports=true;
		grinder.logger.info("before thread.");
	}

	@Before
	public void before() {
<#if commonHeader?? && commonHeader?size != 0>
		/** Common Headers */
		request.setHeaders(nvs([
	<#assign keys = commonHeader?keys>
	<#list keys as key>
			"${key}" : "${commonHeader[key]?replace("$", "\\$")}" <#if key?has_next>,</#if>
	</#list>
		]))
</#if>
		grinder.logger.info("before thread. init headers and cookies");
	}

	@Test
	public void test(){
<#if requests??>
	<#list requests as request>

		def postData_${request_index} = [];
		def perRequestHeaders_${request_index} = [];
		<#if request.postData?? && request.postData?size != 0>
			<#assign keys = request.postData?keys>
		postData_${request_index} = nvs([
			<#list keys as key>
			"${key}" : "${request.postData[key]?replace("$", "\\$")}" <#if key?has_next>,</#if>
			</#list>
		])
		</#if>
		<#if request.headers?? && request.headers?size != 0>
			<#assign keys = request.headers?keys>
		perRequestHeaders_${request_index} = nvs([
			<#list keys as key>
			"${key}" : "${request.headers[key]?replace("$", "\\$")}" <#if key?has_next>,</#if>
			</#list>
		])
		</#if>
		HTTPResponse result_${request_index} = request.${request.method?default("GET")}("${request.url}",postData_${request_index} ,perRequestHeaders_${request_index})
		assertThat(result_${request_index}.statusCode, is(${request.state}));
	</#list>
</#if>

	}

	// Provide a method to convert map to NVPair array
	def nvs(def map) {
		def nvs = []
		map.each {
			key, value ->  nvs.add(new NVPair(key, value))
		}
		return nvs as NVPair[]
	}

}


