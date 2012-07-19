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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.List;

import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Test Service Class.
 * 
 * @author Mavlarn
 * @since 3.0
 */
@Service
public class PerfTestService {

	@Autowired
	private PerfTestRepository perfTestRepository;

	public Page<PerfTest> getTestList(User user, boolean isFinished, Pageable pageable) {
		if (isFinished) {
			return perfTestRepository.findAllByStatusAndCreatedUserOrderByCreatedDateAsc(Status.FINISHED, user,
					pageable);
		} else {
			return perfTestRepository.findAllByCreatedUserOrderByCreatedDateAsc(user, pageable);
		}
	}

	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	public PerfTest savePerfTest(PerfTest test) {
		checkNotNull(test);
		return perfTestRepository.save(test);
	}

	/**
	 * Save performance test with given status
	 * 
	 * @param test
	 * @param status
	 * @return
	 */
	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	public PerfTest savePerfTest(PerfTest test, Status status) {
		checkNotNull(test);
		test.setStatus(status);
		return perfTestRepository.save(test);
	}

	@Cacheable(value = "perftest")
	public PerfTest getPerfTest(long testId) {
		return perfTestRepository.findOne(testId);
	}

	@Cacheable(value = "perftest")
	public PerfTest getPerfTestCandiate() {
		Page<PerfTest> perfTest = perfTestRepository.findAllByStatusOrderByCreatedDateAsc(Status.TESTING,
				new PageRequest(0, 1));
		return (perfTest.getNumber() == 0) ? null : perfTest.getContent().get(0);
	}

	@Cacheable(value = "perftestlist")
	public List<PerfTest> getTestingPerfTest() {
		return perfTestRepository.findAllByStatusOrderByCreatedDateAsc(Status.TESTING);
	}

	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	public void deletePerfTest(long id) {
		perfTestRepository.delete(id);
	}
}
