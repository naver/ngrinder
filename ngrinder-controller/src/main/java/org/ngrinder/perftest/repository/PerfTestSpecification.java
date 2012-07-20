package org.ngrinder.perftest.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.springframework.data.jpa.domain.Specification;

public class PerfTestSpecification {
	public static Specification<PerfTest> statusSetEqual(final Status... statuses) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("status").in((Object[]) statuses);
			}
		};
	}

	public static Specification<PerfTest> createBy(final User user) {
		return new Specification<PerfTest>() {
			@Override
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("createdUser"), user);
			}
		};
	}
}
