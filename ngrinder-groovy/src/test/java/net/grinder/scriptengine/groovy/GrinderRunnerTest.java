/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.grinder.scriptengine.groovy;

import net.grinder.script.GTest;
import net.grinder.script.InvalidContextException;
import net.grinder.script.NonInstrumentableTypeException;
import net.grinder.scriptengine.groovy.junit.GrinderRunner;
import net.grinder.scriptengine.groovy.junit.annotation.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.ngrinder.http.HTTPRequest;
import org.ngrinder.http.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.grinder.script.Grinder.grinder;

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
			request = HTTPRequest.create();
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
			if (result.code() != 200) {
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
			if (result.code() != 200) {
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
