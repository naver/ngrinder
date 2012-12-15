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
package org.ngrinder.perftest.service;

import java.text.ParseException;

import org.junit.Ignore;
import org.junit.Test;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * {@link PerfTest} generation utility for test
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.0
 */
@Ignore("Only enable this when test data is necessary.")
public class AddTestRecord extends AbstractPerfTestTransactionalTest {

	@Autowired
	PerfTestRepository perfTestRepository;

	@Test
	@Rollback(false)
	public void testGetTestListAll() throws ParseException {
		createPerfTest("test1", Status.READY, DateUtil.toSimpleDate("2011-01-01"));
		createPerfTest("test2", Status.READY, DateUtil.toSimpleDate("2011-01-02"));
		createPerfTest("test3", Status.DISTRIBUTE_FILES, DateUtil.toSimpleDate("2011-01-01"));
		createPerfTest("test4", Status.TESTING, DateUtil.toSimpleDate("2011-01-03"));
		createPerfTest("test5", Status.CANCELED, DateUtil.toSimpleDate("2011-01-04"));
		createPerfTest("test6", Status.FINISHED, DateUtil.toSimpleDate("2011-01-05"));
		createPerfTest("test7", Status.FINISHED, DateUtil.toSimpleDate("2011-01-06"));
	}
}
