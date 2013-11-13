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
 * {@link PerfTest} Specification for the more elaborated PerfTest query.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class PerfTestSpecification {

	/**
	 * Get the{@link Specification} checking if the {@link PerfTest} has one of given {@link Status}.
	 *
	 * @param statuses status set
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
	 * Get the {@link Specification} checking if the {@link PerfTest#getTags()} has the given tagValue.
	 *
	 * @param tagValue tagValue
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
	 * Get the {@link Specification} checking if the {@link PerfTest} has one of the given IDs.
	 *
	 * @param ids id list
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
	 * Get the {@link Specification} checking if the {@link PerfTest} has the given ID.
	 *
	 * @param id perf test id
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
	 * Get the {@link Specification} checking if the {@link PerfTest} has the given region.
	 *
	 * @param region region of perf test
	 * @return {@link Specification}
	 * @since 3.1
	 */
	public static Specification<PerfTest> idRegionEqual(final String region) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.equal(root.get("region"), region), cb.equal(root.get("region"), ""));
			}
		};
	}

	/**
	 * Get the {@link Specification} whichs provide empty predicate.
	 *
	 * This is for the base element for "and" or "or" combination.
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
	 * Get the Specification which provide empty predicate for schedule time. This is for the base element for "and" or
	 * "or" combination.
	 *
	 * @return {@link Specification}
	 */
	public static Specification<PerfTest> scheduledTimeNotEmptyPredicate() {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("scheduledTime").isNotNull();
			}
		};
	}

	/**
	 * Get the search {@link Specification} for test name and description fields.
	 *
	 * @param queryString query String
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
	 * Get createBy {@link Specification} to get the {@link PerfTest}s whose creator is the given user.
	 *
	 * @param user user
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
