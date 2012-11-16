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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link PerfTest} Specification for more elaborated search.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class PerfTestSpecification {
	

	/**
	 * Get the Specification checking the {@link PerfTest} has one of given {@link Status}.
	 * 
	 * @param statuses
	 *            status set
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> statusSetEqual(final Status... statuses) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("status").in((Object[]) statuses);
			}
		};
	}

	/**
	 * Get the Specification checking if the {@link PerfTest#getTags()} has the given tagValue.
	 * 
	 * @param tagValue
	 *            tagValue
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> hasTag(final String tagValue) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				SetJoin<Object, Object> join = root.joinSet("tags");
				return cb.equal(join.get("tagValue"), tagValue);
			}
		};
	}
	
	/**
	 * Get the Specification checking if the {@link PerfTest} has one of the given IDs.
	 * 
	 * @param ids
	 *            id list
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> idSetEqual(final Long[] ids) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").in((Object[]) ids);
			}
		};
	}

	/**
	 * Get the Specification checking if the {@link PerfTest} has the given ID.
	 * 
	 * @param id
	 *            perftest id
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> idEqual(final Long id) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("id"), id);
			}
		};
	}

	/**
	 * Get the Specification checking if the {@link PerfTest} has the given region.
	 * 
	 * @since 3.1
	 * @param id
	 *            perftest id
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> idRegionEqual(final String region) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("region"), region);
			}
		};
	}

	/**
	 * Get the Specification which provide empty predicate for id. This is for the base element for "and" or "or" combination.
	 * 
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> idEmptyPredicate() {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}
	
	/**
	 * Get the Specification which provide empty predicate for schedule time. This is for the base element for "and" or "or" combination.
	 * 
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> scheduledTimeEmptyPredicate() {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("scheduledTime").isNotNull();
			}
		};
	}

	/**
	 * Get the search {@link Specification} for testName and description fields.
	 * 
	 * @param queryString
	 *            query String
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> likeTestNameOrDescription(final String queryString) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				String queryStr = ("%" + queryString + "%").toLowerCase();
				return cb.or(cb.like(cb.lower(root.get("testName").as(String.class)), queryStr),
						cb.like(root.get("description").as(String.class), queryStr));
			}
		};
	}

	/**
	 * Get createBy specification to get the {@link PerfTest} whose creator is the given user.
	 * 
	 * @param user
	 *            user
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> createdBy(final User user) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.equal(root.get("createdUser"), user));
			}
		};
	}

}
