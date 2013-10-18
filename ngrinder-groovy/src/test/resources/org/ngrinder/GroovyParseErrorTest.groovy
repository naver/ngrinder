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
class MyTestEx {

	public static GTest test;
	public static GTest test2;
	public static HTTPRequest request;
	public static HTTPRequest request2;

	@BeforeClass
	public static void beforeClass() {
		grinder.getLogger().info("before");
		test = new GTest(1, "Hello");
		test2 = new GTest(2, "Hello");
		request = new HTTPRequest();
		request2 = new HTTPRequest();
		test.record(request);
		test2.record(request2);
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
		HTTPResponse result = request.GET("http://www.google.com");
		if (new Random().nextInt(5) == 1) {
			throw new Exception("Exception test");
		}
		grinder.statistics.forLastTest.success = 1
	}

	@Test
	public void testHello2() {
		def result = request2.GET("http://www.naver.com/");
		grinder.getLogger().info("code: {}", result.statusCode);
		if (result.getStatusCode() != 200) {
			grinder.statistics.forLastTest.success = 0
		} else {
			grinder.statistics.forLastTest.success = 1
		}
	}
}
