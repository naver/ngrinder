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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.trim;
import static org.ngrinder.common.util.AccessUtils.getSafe;

/**
 * User managed by nGrinder.
 *
 * @author Mavlarn
 * @since 3.0
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "NUSER")
public class User extends BaseModel<User> {

	private static final long serialVersionUID = 7398072895183814285L;

	@Column(name = "user_id", unique = true, nullable = false)
	/* User Id */
	private String userId;

	@Column(name = "user_name")
	/* User Name e.g) Jone Dogh. */
	private String userName;

	private String password;

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1)")
	private Boolean enabled;

	private String email;

	@JsonDeserialize(using = RoleDeserializer.class)
	@Enumerated(EnumType.STRING)
	@Column(name = "role_name", nullable = false)
	private Role role;

	private String description;

	private String timeZone;

	@Column(name = "user_language")
	private String userLanguage;

	@Column(name = "is_external", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean external;

	@Column(name = "authentication_provider_class")
	/* Who provide the authentication */
	private String authProviderClass;

	@Transient
	private User follower;

	@Transient
	private String followersStr;

	@Transient
	private User ownerUser;

	@JsonSerialize(using = UserReferenceListSerializer.class)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "SHARED_USER", joinColumns = @JoinColumn(name = "owner_id"), // LF
		inverseJoinColumns = @JoinColumn(name = "follow_id"))
	private List<User> followers;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "SHARED_USER", joinColumns = @JoinColumn(name = "follow_id"), // LF
		inverseJoinColumns = @JoinColumn(name = "owner_id"))
	private List<User> owners;

	/**
	 * Default constructor.
	 */
	public User() {
	}

	/**
	 * Constructor.
	 *
	 * @param userId   user id
	 * @param name     user name
	 * @param password password
	 * @param role     role
	 * @deprecated
	 */
	public User(String userId, String name, String password, Role role) {
		this.userId = userId;
		this.password = password;
		this.userName = name;
		this.role = role;
	}

	@PrePersist
	@PreUpdate
	public void init() {
		this.userId = trim(this.userId);
		this.userName = trim(this.userName);
		this.email = getSafe(trim(this.email), "");
		this.enabled = getSafe(this.enabled, true);
		this.external = getSafe(this.enabled);
		this.role = getSafe(this.role, Role.USER);
	}

	public static User createNew() {
		User user = new User();
		user.init();
		return user;
	}

	/**
	 * Constructor.
	 *
	 * @param userId   user id
	 * @param name     user name
	 * @param password password
	 * @param email    email
	 * @param role     role
	 */
	public User(String userId, String name, String password, String email, Role role) {
		this.userId = userId;
		this.password = password;
		this.userName = name;
		this.email = email;
		this.role = role;
	}

	/**
	 * Check this user is valid.
	 *
	 * @return true if valid
	 */
	public boolean validate() {
		return !(userName == null || email == null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		User other = (User) obj;

		if (userId == null) {
			return other.userId == null;
		}
		return userId.equals(other.userId);
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEmail(String email) {
		this.email = email == null ? "" : email.toLowerCase();
	}

	public boolean isExternal() {
		return getSafe(external);
	}

	@JsonIgnore
	public User getFactualUser() {
		return ownerUser == null ? this : ownerUser;
	}

	/**
	 * Get the user simple information.
	 *
	 * @return user
	 */
	// It will throw StackOverflowException if return User that contains owners and followers value
	// in getCurrentPerfTestStatistics() method.so just return base User info
	@JsonIgnore
	public User getUserBaseInfo() {
		User userInfo = new User();
		userInfo.setId(this.getId());
		userInfo.setUserId(this.getUserId());
		userInfo.setUserName(this.getUserName());
		userInfo.setEmail(this.getEmail());
		return userInfo;
	}

	/**
	 * string representation of User object.
	 *
	 * @return User object information String.
	 */
	// avoid lazy initialization issues ,method toString not contain followers and owners
	@Override
	public String toString() {
		return "User[id=" + this.getId() + ",userId=" + this.getUserId() +
			",userName=" + this.getUserName() + ",role=" + this.getRole() +
			",e-mail=" + this.getEmail() + "]";
	}

	private static class UserReferenceListSerializer extends StdSerializer<List<User>> {
		@SuppressWarnings("unused")
		UserReferenceListSerializer() {
			this(null);
		}

		UserReferenceListSerializer(Class<List<User>> t) {
			super(t);
		}

		@Override
		public void serialize(List<User> followers, JsonGenerator generator, SerializerProvider provider) throws IOException {
			List<User> userBaseInfoList = followers.stream()
				.map(User::getUserBaseInfo)
				.collect(toList());
			generator.writeObject(userBaseInfoList);
		}
	}

	public static class UserReferenceSerializer extends StdSerializer<User> {
		@SuppressWarnings("unused")
		UserReferenceSerializer() {
			this(null);
		}

		UserReferenceSerializer(Class<User> t) {
			super(t);
		}

		@Override
		public void serialize(User user, JsonGenerator generator, SerializerProvider provider) throws IOException {
			generator.writeObject(user.getUserBaseInfo());
		}
	}

	public static class RoleDeserializer extends StdDeserializer<Role> {
		@SuppressWarnings("unused")
		RoleDeserializer() {
			this(null);
		}

		RoleDeserializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public Role deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			JsonNode node = parser.getCodec().readTree(parser);
			TreeNode nameNode = node.get("name");
			if (nameNode == null) {
				return null;
			}

			String name = node.get("name").asText();
			return Arrays.stream(Role.values())
				.filter(role -> role.name().equals(name))
				.findFirst()
				.orElse(null);
		}
	}
}
