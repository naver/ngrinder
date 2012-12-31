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
package org.ngrinder.agent.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.grinder.message.console.AgentControllerState;

import org.ngrinder.agent.model.AgentInfo;
import org.springframework.data.jpa.domain.Specification;

/**
 * Agent Manager JPA Specification.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
public abstract class AgentManagerSpecification {

	/**
	 * Query specification which the region column start the specified region.
	 * 
	 * @param region
	 *            specified region to query
	 * @return Specification of this query
	 */
	public static Specification<AgentInfo> startWithRegion(final String region) {
		return new Specification<AgentInfo>() {
			@Override
			public Predicate toPredicate(Root<AgentInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<String> regionField = root.get("region").as(String.class);
				return cb.or(cb.like(regionField, region + "_%"), cb.equal(regionField, region));
			}
		};
	}

	/**
	 * Query specification which the status is active.
	 * 
	 * @return Specification of this query
	 */
	public static Specification<AgentInfo> active() {
		return new Specification<AgentInfo>() {
			@Override
			public Predicate toPredicate(Root<AgentInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<AgentControllerState> status = root.get("status").as(AgentControllerState.class);
				return cb.and(cb.notEqual(status, AgentControllerState.INACTIVE),
								cb.notEqual(status, AgentControllerState.UNKNOWN),
								cb.notEqual(status, AgentControllerState.WRONG_REGION));
			}
		};
	}

	/**
	 * Query specification which the status is visible. visible means.. it's visible in the agent
	 * monitor.
	 * 
	 * @return Specification of this query
	 */
	public static Specification<AgentInfo> visible() {
		return new Specification<AgentInfo>() {
			@Override
			public Predicate toPredicate(Root<AgentInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<AgentControllerState> status = root.get("status").as(AgentControllerState.class);
				return cb.notEqual(status, AgentControllerState.INACTIVE);
			}
		};
	}
}
