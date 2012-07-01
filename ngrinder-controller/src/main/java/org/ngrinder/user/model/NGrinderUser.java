package org.ngrinder.user.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ngrinder.model.BaseModel;
import org.springframework.data.jpa.domain.Specification;

/**
 * The Class NGrinderUser.
 * 
 * @author Mavlarn, JunHo Yoon
 */
@Entity
@Table(name = "USER")
public class NGrinderUser extends BaseModel {

	/**
	 * UUID
	 */
	private static final long serialVersionUID = 9067916343854745659L;

	private String userId;

	private String psw;

	private String name;

	private boolean enabled;

	private String email;

	private String role;

	private String description;

	private String timeZone;

	private String userLanguage;

	/**
	 * 부서장 여부
	 */
	private String deptBsYN;
	private String deptCd;
	private String empId;
	private String mobilePhone;

	public NGrinderUser() {

	}

	public NGrinderUser(String userId, String name, String password, String userRole) {
		this.userId = userId;
		this.psw = password;
		this.name = name;
		this.role = userRole;
		isEnabled();
	}

	public String getDeptBsYN() {
		return deptBsYN;
	}

	public void setDeptBsYN(String deptBsYN) {
		this.deptBsYN = deptBsYN;
	}

	public String getDeptCd() {
		return deptCd;
	}

	public void setDeptCd(String deptCd) {
		this.deptCd = deptCd;
	}

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
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

	public String getPsw() {
		return psw;
	}

	public void setPsw(String password) {
		this.psw = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String userRole) {
		this.role = userRole;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public static Specification<NGrinderUser> nameLike(final String query) {
		return new Specification<NGrinderUser>() {

			@Override
			public Predicate toPredicate(Root<NGrinderUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
				String pattern = "%" + query + "%";
				return cb.like(root.get("name").as(String.class), pattern);
			}
		};
	}

	public static Specification<NGrinderUser> emailLike(final String query) {
		return new Specification<NGrinderUser>() {
			@Override
			public Predicate toPredicate(Root<NGrinderUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
				String pattern = "%" + query + "%";
				return cb.like(root.get("email").as(String.class), pattern);
			}
		};
	}
}
