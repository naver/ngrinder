package org.ngrinder.common.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.ngrinder.common.util.LoggingUtils.format;

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
public class LoggingUtilsTest {

	private static PerfTest perfTest;

	@BeforeClass
	public static void init() {
		User user = new User();
		user.setUserId("format test user");

		perfTest = new PerfTest();
		perfTest.setId(12345678L);
		perfTest.setCreatedBy(user);
	}

	@Test
	public void formatTest() {
		assertThat(format(perfTest, "Test is started."), is("[12345678][format test user] Test is started."));
		assertThat(format(perfTest, "{} test is {} started at {}.", "Beautiful", 777, 113.44544),
			is("[12345678][format test user] Beautiful test is 777 started at 113.44544."));

		assertThat(format(null, "Test is started."), is("Test is started."));

		perfTest.setCreatedBy(null);
		assertThat(format(perfTest, "Test is started."), is("Test is started."));
	}
}
