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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.grinder.message.console.AgentControllerState;

import org.ngrinder.agent.model.AgentInfo;
import org.springframework.data.jpa.domain.Specification;

/**
 * Agent Manager JPA Specification/
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
				String queryStr = region + "_%";
				Expression<String> regionField = root.get("region").as(String.class);
				return cb.or(cb.like(regionField, queryStr), cb.equal(regionField, region));
			}
		};
	}

	/**
	 * Query specification which the region column start the specified region.
	 * 
	 * @param region
	 *            specified region to query
	 * @return Specification of this query
	 */
	public static Specification<AgentInfo> active() {
		return new Specification<AgentInfo>() {
			@Override
			public Predicate toPredicate(Root<AgentInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<AgentControllerState> status = root.get("status").as(AgentControllerState.class);
				return cb.notEqual(status, AgentControllerState.INACTIVE);
			}
		};
	}
}
