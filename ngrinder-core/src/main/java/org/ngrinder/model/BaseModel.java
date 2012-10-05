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
package org.ngrinder.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Index;

/**
 * Base Model which has following attribute.
 * <ul>
 * <li>created_user</li>
 * <li>created_date</li>
 * <li>lastmodified_user</li>
 * <li>lastmodified_date</li>
 * </ul>
 * 
 * @param <M>
 *            wrapped entity
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
@MappedSuperclass
public class BaseModel<M> extends BaseEntity<M> {

	private static final long serialVersionUID = -3876339828833595694L;

	@Column(name = "created_date", insertable = true, updatable = false)
	private Date createdDate;

	@ManyToOne
	@JoinColumn(name = "created_user", insertable = true, updatable = false)
	@Index(name = "created_user_index")
	private User createdUser;

	@Column(name = "last_modified_date", insertable = true, updatable = true)
	private Date lastModifiedDate;

	@ManyToOne
	@JoinColumn(name = "last_modified_user", insertable = true, updatable = true)
	@Index(name = "last_modified_user_index")
	private User lastModifiedUser;

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public User getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(User createdUser) {
		this.createdUser = createdUser;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
}
