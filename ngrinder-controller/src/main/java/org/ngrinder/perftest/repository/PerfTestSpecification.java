package org.ngrinder.perftest.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link PerfTest} Specification for more elaborated search.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class PerfTestSpecification {
	public static Specification<PerfTest> statusSetEqual(final Status... statuses) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("status").in((Object[]) statuses);
			}
		};
	}

	public static Specification<PerfTest> emptyPredicate() {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}

	public static Specification<PerfTest> likeTestNameOrDescription(final String queryString) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.like(root.get("testName").as(String.class), "%" + queryString + "%"),
						cb.like(root.get("description").as(String.class), "%" + queryString + "%"));
			}
		};
	}

	public static Specification<PerfTest> createdBy(final User user) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("createdUser"), user);
			}
		};
	}

	public static Sort sortByCreatedDate() {
		return new Sort(Sort.Direction.ASC, "createdDate");
	}

	public static Specification<PerfTest> isFinsihed() {
		// TODO Auto-generated method stub
		return null;
	}
}
