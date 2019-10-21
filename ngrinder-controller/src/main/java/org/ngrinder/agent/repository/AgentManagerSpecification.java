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

import org.ngrinder.model.AgentInfo;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Agent Manager JPA Specification.
 *
 * @since 3.1
 */
public abstract class AgentManagerSpecification {

	/**
	 * Query specification to query the agent existing in the specified region.
	 *
	 * @param region region to query
	 * @return Specification of this query
	 */
	public static Specification<AgentInfo> startWithRegion(final String region) {
		return (Specification<AgentInfo>) (root, query, cb) -> {
			Expression<String> regionField = root.get("region").as(String.class);
			return cb.or(cb.like(regionField, region + "/_owned%", cb.literal('/')), cb.equal(regionField,
					region));
		};
	}

	/**
	 * Get the {@link Specification} checking if the {@link AgentInfo} has the given ID.
	 *
	 * @param id agent id
	 * @return {@link Specification}
	 */
	public static Specification<AgentInfo> idEqual(final Long id) {
		return (Specification<AgentInfo>) (root, query, cb) -> cb.equal(root.get("id"), id);
	}
	
}
