package com.nhncorp.ngrinder.test.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nhncorp.ngrinder.core.model.BaseEntity;
import com.nhncorp.ngrinder.script.model.Script;

/**
 * test params entity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Entity
@Table(name = "TEST_PARAMS")
public class TestParams extends BaseEntity {

	private static final long serialVersionUID = 83379827758499899L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SCRIPT_ID")
	private Script script;

	private int vusers;

	private long runDuration;

	private long runCount;

	private long sampleInterval;

	private long ignoreCount;

	private boolean monitor = false;

	/**
	 * 0: run only once <br>
	 * !0: always run
	 */
	private long runInterval;

	// other...

}
