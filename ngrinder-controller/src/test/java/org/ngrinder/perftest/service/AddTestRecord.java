/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
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
package org.ngrinder.perftest.service;

import java.text.ParseException;

import org.junit.Ignore;
import org.junit.Test;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
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
