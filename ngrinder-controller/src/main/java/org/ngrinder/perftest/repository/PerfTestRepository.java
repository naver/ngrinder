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
package org.ngrinder.perftest.repository;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * {@link PerfTest} Repository.
 *
 * @since 3.0
 */
public interface PerfTestRepository extends JpaRepository<PerfTest, Long>, JpaSpecificationExecutor<PerfTest> {
	/**
	 * Find the paged {@link PerfTest}s based on the given spec.
	 *
	 * @param spec     {@link Specification} of {@link PerfTest} query
	 * @param pageable page info
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAll(Specification<PerfTest> spec, Pageable pageable);


	/**
	 * Find all {@link PerfTest}s having the given {@PerfTest#status} ordered by scheduledTime
	 * ascending.
	 *
	 * @param status status
	 * @return {@link PerfTest} list
	 */
	List<PerfTest> findAllByStatusOrderByScheduledTimeAsc(Status status);

	/**
	 * Find all {@link PerfTest}s having the given {@PerfTest#status} and {@PerfTest#region
	 * } ordered by scheduledTime ascending.
	 *
	 * @param status perf test status to search
	 * @param region region where the test belong to
	 * @return perf test list
	 */
	List<PerfTest> findAllByStatusAndRegionOrderByScheduledTimeAsc(Status status, String region);

	/**
	 * Find all {@link PerfTest}s created between the given start and end date and having the the given region.
	 *
	 * @param start  time
	 * @param end    time
	 * @param region region
	 * @return {@link PerfTest} list
	 */
	@Query("select p from PerfTest  p where p.startTime between ?1 and ?2 and region=?3")
	List<PerfTest> findAllByCreatedAtAndRegion(Date start, Date end, String region);

	/**
	 * Find all {@link PerfTest} created between the given start and end dates.
	 *
	 * @param start time
	 * @param end   time
	 * @return {@link PerfTest} list
	 */
	@Query("select p from PerfTest  p where p.startTime between ?1 and ?2")
	List<PerfTest> findAllByCreatedAt(Date start, Date end);
}
