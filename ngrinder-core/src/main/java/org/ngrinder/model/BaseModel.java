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
package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Base Model which has following attribute.
 *
 * @param <M> wrapped entity
 * @since 3.0
 */
@Getter
@Setter
@MappedSuperclass
public class BaseModel<M> extends BaseEntity<M> {

	private static final long serialVersionUID = -3876339828833595694L;

	@Column(name = "created_date", insertable = true, updatable = false)
	private Instant createdDate;

	@JsonSerialize(using = User.UserReferenceSerializer.class)
	@ManyToOne
	@JoinColumn(name = "created_user", insertable = true, updatable = false)
	@Index(name = "created_user_index")
	@NotFound(action = NotFoundAction.IGNORE)
	private User createdUser;

	@Column(name = "last_modified_date", insertable = true, updatable = true)
	private Instant lastModifiedDate;

	@JsonSerialize(using = User.UserReferenceSerializer.class)
	@ManyToOne
	@JoinColumn(name = "last_modified_user", insertable = true, updatable = true)
	@Index(name = "last_modified_user_index")
	@NotFound(action = NotFoundAction.IGNORE)
	private User lastModifiedUser;

}
