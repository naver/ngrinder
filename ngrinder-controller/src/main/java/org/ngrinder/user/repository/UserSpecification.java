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
package org.ngrinder.user.repository;

import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * User entity Specification Holder.
 */
public abstract class UserSpecification {

	private UserSpecification() {
	}

	/**
	 * Create "Name Like" query spec.
	 * @param query query
	 * @return created spec
	 */
	public static Specification<User> nameLike(final String query) {
		return (Specification<User>) (root, criteriaQuery, cb) -> {
			String pattern = ("%" + query + "%").toLowerCase();
			return cb.or(cb.like(cb.lower(root.get("userName").as(String.class)), pattern),
					cb.like(cb.lower(root.get("userId").as(String.class)), pattern));
		};
	}

	/**
	 * Create "Email like" query spec.
	 * @param query query
	 * @return created spec
	 */
	public static Specification<User> emailLike(final String query) {
		return (Specification<User>) (root, criteriaQuery, cb) -> {
			String pattern = ("%" + query + "%").toLowerCase();
			return cb.like(cb.lower(root.get("email").as(String.class)), pattern);
		};
	}

	/**
	 * Get the {@link Specification} checking if the {@link User} has the given ID.
	 *
	 * @param id User id
	 * @return {@link Specification}
	 */
	public static Specification<User> idEqual(final Long id) {
		return (Specification<User>) (root, query, cb) -> cb.equal(root.get("id"), id);
	}

}
