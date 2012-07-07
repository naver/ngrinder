package org.ngrinder.user.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
	public static Specification<User> nameLike(final String query) {
		return new Specification<User>() {
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
				String pattern = "%" + query + "%";
				return cb.like(root.get("userName").as(String.class), pattern);
			}
		};
	}

	public static Specification<User> emailLike(final String query) {
		return new Specification<User>() {
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
				String pattern = "%" + query + "%";
				return cb.like(root.get("email").as(String.class), pattern);
			}
		};
	}

}
