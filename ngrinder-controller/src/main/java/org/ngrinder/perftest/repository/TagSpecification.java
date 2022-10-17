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

import javax.persistence.criteria.SetJoin;

import org.apache.commons.lang.StringUtils;

import org.ngrinder.model.Tag;
import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link Tag} Specification for more elaborated {@link org.ngrinder.model.PerfTest} search.
 *
 * @since 3.0
 */
public abstract class TagSpecification {


	/**
	 * Get the {@link Specification} which checks if the {@link Tag#getTagValue()} has one of given value.
	 *
	 * @param values tag lists
	 * @return {@link Specification}
	 */
	public static Specification<Tag> valueIn(final String[] values) {
		return (Specification<Tag>) (root, query, cb) -> root.get("tagValue").in((Object[]) values);
	}

	/**
	 * Get lastModifiedBy and createBy {@link Specification} to get the {@link Tag} whose creator or last modifier is
	 * the given user.
	 *
	 * @param user user
	 * @return {@link Specification}
	 */
	public static Specification<Tag> lastModifiedOrCreatedBy(final User user) {
		return (Specification<Tag>) (root, query, cb) -> cb.or(cb.equal(root.get("lastModifiedBy"), user), cb.equal(root.get("createdBy"), user));
	}

	/**
	 * Get the {@link Specification} which checks if the tag has corresponding perfTests.
	 *
	 * @return {@link Specification}
	 */
	public static Specification<Tag> hasPerfTest() {
		return (Specification<Tag>) (root, query, cb) -> {
			SetJoin<Object, Object> join = root.joinSet("perfTests");
			query.groupBy(root.get("id"));
			return join.get("id").isNotNull();
		};
	}

	/**
	 * Get the {@link Specification} to get the {@link Tag} whose value starts with given query.
	 *
	 * @param queryString matching tag value
	 * @return {@link Specification}
	 */
	public static Specification<Tag> isStartWith(final String queryString) {
		return (Specification<Tag>) (root, query, cb) -> {
			String replacedQueryString = StringUtils.replace(queryString, "%", "\\%");
			return cb.like(cb.lower(root.get("tagValue").as(String.class)),
					StringUtils.lowerCase(replacedQueryString) + "%");
		};
	}

}
