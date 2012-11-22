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
package org.ngrinder.agent.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ngrinder.agent.model.AgentInfo;
import org.springframework.data.jpa.domain.Specification;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since 3.1
 */
public abstract class AgentManagerSpecification {

	public static Specification<AgentInfo> startWithRegion(final String region) {
		return new Specification<AgentInfo>() {
			@Override
			public Predicate toPredicate(Root<AgentInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				String queryStr = region + "%";
				return cb.like(root.get("region").as(String.class), queryStr);
			}
		};
	}

	/**
	 * query specification to get available agents for user.
	 * condition is:
			1. the ready agents in this region
			2. user specified agent, which name is: ${region} | "_" + {anykeywork} + "owned_${userId}"
	 * @param region
	 * 				agent region.
	 * @param status
	 * 				agent status
	 * @param user
	 * 				specified user.
	 * @return
	 */
//	public static Specification<AgentInfo> startWithRegionEqualStatusOfUser(final String region,
//			final AgentControllerState status, final User user) {
//		return new Specification<AgentInfo>() {
//			@Override
//			public Predicate toPredicate(Root<AgentInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//				String regionQueryStr = region + "%";
//				String userQueryStr = region + "%owned_" + user.getUserId() + "%";
//				
//				return cb.and(cb.or(cb.like(root.get("region").as(String.class), regionQueryStr),
//									cb.like(root.get("region").as(String.class), userQueryStr)),
//						cb.equal(root.get("status"), status));
//			}
//		};
//	}
}
