/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
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
package net.grinder.scriptengine.groovy;

import static net.grinder.script.Grinder.grinder;
import net.grinder.plugin.http.HTTPRequest;
import net.grinder.script.GTest;
import net.grinder.script.InvalidContextException;
import net.grinder.script.NonInstrumentableTypeException;
import net.grinder.scriptengine.groovy.junit.GrinderRunner;
import net.grinder.scriptengine.groovy.junit.annotation.AfterProcess;
import net.grinder.scriptengine.groovy.junit.annotation.AfterThread;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread;
import net.grinder.scriptengine.groovy.junit.annotation.Repeat;
import net.grinder.scriptengine.groovy.junit.annotation.RunRate;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import HTTPClient.HTTPResponse;

/**
 * Grinder Runner Test
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.2
 */
public class GrinderRunnerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GrinderRunnerTest.class);

	@Test
	public void testThreadInitialization() throws Throwable {
		GrinderRunner runner = new GrinderRunner(TestSample.class);
		LOGGER.debug("start the test function...");
		runner.run(new RunNotifier() {
			@Override
			public void fireTestFailure(Failure failure) {
				throw new RuntimeException(failure.getException());
			}
		});
	}

	@Ignore
	@RunWith(GrinderRunner.class)
	@Repeat(100)
	public static class TestSample {
		private static HTTPRequest request = null;
		private static GTest test = new GTest(1, "Hello");

		@BeforeClass
		public static void beforeProcess() {
			request = new HTTPRequest();
			try {
				test.record(request);
			} catch (NonInstrumentableTypeException e) {
			}
		}

		@BeforeThread
		public void beforeThread() throws InvalidContextException {
			grinder.getStatistics().setDelayReports(true);
		}

		@RunRate(50)
		@Test
		public void doTest() throws Exception {
			HTTPResponse result = request.GET("http://www.naver.com");
			if (result.getStatusCode() != 200) {
				grinder.getStatistics().getForLastTest().setSuccess(false);
			} else {
				grinder.getStatistics().getForLastTest().setSuccess(true);
			}
		}

		@Test
		@RunRate(10)
		public void doTest2() throws Exception {
			grinder.getStatistics().setDelayReports(true);
			HTTPResponse result = request.GET("http://www.google.co.kr");
			if (result.getStatusCode() != 200) {
				grinder.getStatistics().getForLastTest().setSuccess(false);
			} else {
				grinder.getStatistics().getForLastTest().setSuccess(true);
			}
		}

		@AfterThread
		public void doAfter() {
		}

		@AfterProcess
		public static void afterProcess() {
		}
	}

}
