package com.nhncorp.ngrinder.model;

/**
 * The Class NGrinderUser.
 * 
 * @author Mavlarn
 */
public class NGrinderUserBean {

	private String userId;

	private String psw;

	private String userName;

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

	public NGrinderUserBean(String userId, String userName, String password, String userRole) {
		this.userId = userId;
		this.psw = password;
		this.userName = userName;
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

	public NGrinderUserBean() {
		super();
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("userId:").append(userId);
		sb.append(", userName:").append(userName);
		sb.append(", Role:").append(role);
		sb.append(", timeZone:").append(timeZone);
		sb.append(", userLanguage:").append(userLanguage);
		sb.append(", Email:").append(email);
		sb.append(", enabled:").append(enabled);

		return sb.toString();
	}
}
