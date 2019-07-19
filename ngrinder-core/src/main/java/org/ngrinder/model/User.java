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

import static java.util.stream.Collectors.toList;
import static org.ngrinder.common.util.AccessUtils.getSafe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;

/**
 * User managed by nGrinder.
 *
 * @author Mavlarn
 * @since 3.0
 */
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "NUSER")
public class User extends BaseModel<User> {

	private static final long serialVersionUID = 7398072895183814285L;

	@Column(name = "user_id", unique = true, nullable = false)
	/** User Id */
	private String userId;

	@Column(name = "user_name")
	/** User Name e.g) Jone Dogh. */
	private String userName;

	private String password;

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1)")
	private Boolean enabled;

	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_name", nullable = false)
	private Role role;

	private String description;

	private String timeZone;

	@Column(name = "user_language")
	private String userLanguage;

	@Column(name = "mobile_phone")
	private String mobilePhone;

	@Column(name = "is_external", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean external;

	@Column(name = "authentication_provider_class")
	/** Who provide the authentication */
	private String authProviderClass;

	@Transient
	private User follower;

	@Transient
	private String followersStr;

	@Transient
	private User ownerUser;

	@JsonSerialize(using = FollowerSerializer.class)
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
		isEnabled();
	}

	@PrePersist
	@PreUpdate
	public void init() {
		this.userId = StringUtils.trim(this.userId);
		this.userName = StringUtils.trim(this.userName);
		this.email = StringUtils.trim(this.email);
		this.mobilePhone = StringUtils.trim(this.mobilePhone);
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
		isEnabled();
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
			if (other.userId != null) {
				return false;
			}
		} else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getUserLanguage() {
		return userLanguage;
	}

	public void setUserLanguage(String userLanguage) {
		this.userLanguage = userLanguage;
	}

	public boolean isExternal() {
		return getSafe(external);
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public String getAuthProviderClass() {
		return authProviderClass;
	}

	public void setAuthProviderClass(String authProviderClass) {
		this.authProviderClass = authProviderClass;
	}

	public List<User> getFollowers() {
		return followers;
	}

	public void setFollowers(List<User> followers) {
		this.followers = followers;
	}

	public List<User> getOwners() {
		return owners;
	}

	public void setOwners(List<User> owners) {
		this.owners = owners;
	}

	public User getOwnerUser() {
		return ownerUser;
	}

	public void setOwnerUser(User ownerUser) {
		this.ownerUser = ownerUser;
	}

	public User getFollower() {
		return follower;
	}

	public void setFollower(User follower) {
		this.follower = follower;
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
		return "User[ID=" + this.getId() + ",name=" + this.getUserId() + ",Role=" + this.getRole() + "]";
	}

	public String getFollowersStr() {
		return followersStr;
	}

	public void setFollowersStr(String followersStr) {
		this.followersStr = followersStr;
	}

	private static class FollowerSerializer extends StdSerializer<List<User>> {
		@SuppressWarnings("unused")
		FollowerSerializer() {
			this(null);
		}

		FollowerSerializer(Class<List<User>> t) {
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

	public static class UserBaseInfoSerializer extends StdSerializer<User> {
		@SuppressWarnings("unused")
		UserBaseInfoSerializer() {
			this(null);
		}

		UserBaseInfoSerializer(Class<User> t) {
			super(t);
		}

		@Override
		public void serialize(User user, JsonGenerator generator, SerializerProvider provider) throws IOException {
			generator.writeObject(user.getUserBaseInfo());
		}
	}
}
