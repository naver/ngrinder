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
package org.ngrinder.perftest.repository;

import java.util.List;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * {@link PerfTest} Repository.
 * 
 * @author junHo Yoon
 * @since 3.0
 */
public interface PerfTestRepository extends JpaRepository<PerfTest, Long>, JpaSpecificationExecutor<PerfTest> {
	/**
	 * Find all {@link PerfTest} based on spec.
	 * 
	 * @param spec
	 *            {@link Specification} of {@link PerfTest} query
	 * @param pageable
	 *            page info
	 * 
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAll(Specification<PerfTest> spec, Pageable pageable);

	/**
	 * Find all {@link PerfTest} based on user.
	 * 
	 * @param user
	 *            user
	 * @param pageable
	 *            page info
	 * 
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAllByCreatedUserOrderByCreatedDateAsc(User user, Pageable pageable);

	/**
	 * Find all {@link PerfTest} based on {@link Status} ordered by CreatedDate.
	 * 
	 * @param status
	 *            status
	 * 
	 * @return {@link PerfTest} list
	 */
	List<PerfTest> findAllByStatusOrderByCreatedDateAsc(Status status);

	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} ordered by CreatedDate.
	 * 
	 * @param status
	 *            status
	 * @param pageable
	 *            page info
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAllByStatusOrderByCreatedDateAsc(Status status, Pageable pageable);

	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} ordered by scheduledTime ascending.
	 * 
	 * @param status
	 *            status
	 * @param pageable
	 *            page info
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAllByStatusOrderByScheduledTimeAsc(Status status, Pageable pageable);

	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} ordered by scheduledTime ascending.
	 * 
	 * @param status
	 *            status
	 * @return {@link PerfTest} list
	 */
	List<PerfTest> findAllByStatusOrderByScheduledTimeAsc(Status status);
	
	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} and {@PerfTest#region} ordered by
	 * scheduledTime ascending.
	 * 
	 * @param status
	 * @return
	 */
	List<PerfTest> findAllByStatusAndRegionOrderByScheduledTimeAsc(Status status);
}
