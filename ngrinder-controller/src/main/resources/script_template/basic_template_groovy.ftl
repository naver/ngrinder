package org.ngrinder;

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import HTTPClient.HTTPResponse

@RunWith(GrinderRunner)
class MyTest {
	public static GTest test;
	public static HTTPRequest request;

	@BeforeClass
	public static void beforeClass() {
		test = new GTest(1, "Hello");
		request = new HTTPRequest();
		test.record(request);
	}

	@Before
	public void before() {
		grinder.statistics.delayReports=true;
	}

	@BeforeThread
	public void beforeThread() {
		grinder.statistics.delayReports=true;
		grinder.getLogger().info("before thread in MyTest.");
	}

	@Test
	public void testHello(){
		HTTPResponse result = request.GET("${url}");

		if (result.getStatusCode() == 200) {
			grinder.statistics.forLastTest.success = 1;
		} else if (result.getStatusCode() == 301 || result.getStatusCode() == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.getStatusCode()); 
			grinder.statistics.forLastTest.success = 1;
		} else {
			grinder.statistics.forLastTest.success = 0;
		}
	}
}
