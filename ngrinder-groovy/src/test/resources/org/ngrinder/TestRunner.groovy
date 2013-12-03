package org.ngrinder;

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread

import org.hamcrest.Matchers;
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import HTTPClient.HTTPResponse

@RunWith(GrinderRunner)
class TestRunner {
    public static HTTPRequest request;
    public static int callCount1 = 0;
    public static int callCount2 = 0;

    @BeforeProcess
    public static void beforeProcess() {
        request = new HTTPRequest();
    }

    @BeforeThread
    public void beforeThread() {
        grinder.statistics.delayReports = true
        grinder.getLogger().info("before thread in MyTest.");
        new GTest(1, "Hello").record(request);
    }

    @Before
    public void before() {
        grinder.getLogger().info("before in MyTest.");
    }

    @Test
    public void testHello() {
        callCount1++;
        grinder.getLogger().info("testHello.");
    }

    @Test
    public void testHello2() {
        callCount2++;
    }
}
