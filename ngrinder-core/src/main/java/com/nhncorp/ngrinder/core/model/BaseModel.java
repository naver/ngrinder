package com.nhncorp.ngrinder.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Base Model
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@MappedSuperclass

public class BaseModel extends BaseEntity {
	private static final long serialVersionUID = -3876339828833595694L;

	@Column(name = "CREATE_DATE", nullable = false, insertable = true, updatable = false)
	private Date createDate;

	@Column(name = "CREATE_USER", nullable = false, insertable = true, updatable = false)
	private String createUser;

	@Column(name = "LAST_MODIFIED_DATE", nullable = false)
	private Date lastModifiedDate;

	@Column(name = "LAST_MODIFIED_USER", nullable = false)
	private String lastModifiedUser;

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(String lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
}
