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

import org.apache.commons.lang.StringUtils;

import org.ngrinder.model.Tag;
import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link Tag} Specification for more elaborated {@link org.ngrinder.model.PerfTest} search.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class TagSpecification {

	/**
	 * Get the Specification which provides empty predicate.<br/>
	 * This is for the base element for "and" or "or" combination.
	 * 
	 * @return {@link Specification}
	 */
	public static Specification<Tag> emptyPredicate() {
		return new Specification<Tag>() {
			@Override
			public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}

	/**
	 * Get the Specification which checks the {@link Tag#getTagValue()} has one of given value.
	 * 
	 * @param values
	 *            tag lists
	 * @return {@link Specification}
	 */
	public static Specification<Tag> valueIn(final String[] values) {
		return new Specification<Tag>() {
			@Override
			public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("tagValue").in((Object[]) values);
			}
		};
	}

	/**
	 * Get lastModifiedUser and createBy specification to get the {@link Tag} whose creator or last
	 * modifier is the given user.
	 * 
	 * @param user
	 *            user
	 * @return {@link Specification}
	 */
	public static Specification<Tag> lastModifiedOrCreatedBy(final User user) {
		return new Specification<Tag>() {
			@Override
			public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.equal(root.get("lastModifiedUser"), user), cb.equal(root.get("createdUser"), user));
			}
		};
	}

	/**
	 * Get the Specification which checks the tag has corresponding perfTest.
	 * 
	 * @return {@link Specification}
	 */
	public static Specification<Tag> hasPerfTest() {
		return new Specification<Tag>() {
			@Override
			public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				SetJoin<Object, Object> join = root.joinSet("perfTests");
				query.groupBy(root.get("id"));
				return join.get("id").isNotNull();
			}
		};
	}

	/**
	 * Get query specification to get the {@link Tag} whose value starts with
	 * given query.
	 * 
	 * @param queryString
	 *            matching tag value
	 * @return {@link Specification}
	 */
	public static Specification<Tag> isStartWith(final String queryString) {
		return new Specification<Tag>() {
			@Override
			public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				String replacedQueryString = StringUtils.replace(queryString, "%", "\\%");
				return cb.like(cb.lower(root.get("tagValue").as(String.class)),
								StringUtils.lowerCase(replacedQueryString) + "%");
			}
		};
	}

}
