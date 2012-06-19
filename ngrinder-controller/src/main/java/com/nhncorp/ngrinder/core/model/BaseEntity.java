package com.nhncorp.ngrinder.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base Entity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@MappedSuperclass
public class BaseEntity implements Serializable {

	private static final long serialVersionUID = 8571113820348514692L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false, insertable = true, updatable = false)
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		if (this.id == 0) {
			this.id = id;
		}
	}
}
