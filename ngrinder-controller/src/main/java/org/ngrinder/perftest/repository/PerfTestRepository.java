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

import java.util.Date;
import java.util.List;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
	 * Find all {@link PerfTest} based on {@PerfTest#status} ordered by
	 * CreatedDate.
	 * 
	 * @param status
	 *            status
	 * @param pageable
	 *            page info
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAllByStatusOrderByCreatedDateAsc(Status status, Pageable pageable);

	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} ordered by
	 * scheduledTime ascending.
	 * 
	 * @param status
	 *            status
	 * @param pageable
	 *            page info
	 * @return {@link PerfTest} list
	 */
	Page<PerfTest> findAllByStatusOrderByScheduledTimeAsc(Status status, Pageable pageable);

	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} ordered by
	 * scheduledTime ascending.
	 * 
	 * @param status
	 *            status
	 * @return {@link PerfTest} list
	 */
	List<PerfTest> findAllByStatusOrderByScheduledTimeAsc(Status status);

	/**
	 * Find all {@link PerfTest} based on {@PerfTest#status} and
	 * {@PerfTest#region} ordered by scheduledTime ascending.
	 * 
	 * @param status
	 *            perf test status to search
	 * @param region
	 *            region where the test belong to
	 * @return perf test list
	 */
	List<PerfTest> findAllByStatusAndRegionOrderByScheduledTimeAsc(Status status, String region);

	/**
	 * Update the runtime statistics on the given {@link PerfTest} id.
	 * 
	 * @param id
	 *            {@link PerfTest} id
	 * @param runningSample
	 *            runningSample json string
	 * @param agentStatus
	 *            agentStatus json string
	 * @return the count of updated row
	 */
	@Modifying
	@Query("update PerfTest p set p.runningSample=?2, p.agentStatus=?3 where p.id=?1")
	int updateRuntimeStatistics(Long id, String runningSample, String agentStatus);

	/**
	 * Update the monitor statistics on the given {@link PerfTest} id.
	 * 
	 * @param id
	 *            {@link PerfTest} id
	 * @param monitorStatus
	 *            monitor status json string
	 * @return the count of updated row
	 */
	@Modifying
	@Query("update PerfTest p set p.monitorStatus=?2 where p.id=?1")
	int updatetMonitorStatus(Long id, String monitorStatus);

	/**
	 * Find all {@link PerfTest} based on given start and end times.
	 * 
	 * @param start
	 *            time
	 * @param end
	 *            time
	 * 
	 * @return {@link PerfTest} list
	 */
	@Query("select p from PerfTest  p where p.startTime between ?1 and ?2")
	List<PerfTest> findAllByGivenTimeRegion(Date start, Date end);
}
