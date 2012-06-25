package com.nhncorp.ngrinder.test.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nhncorp.ngrinder.core.model.BaseEntity;

/**
 * test result entity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Entity
@Table(name = "TEST_RESULT")
public class TestResult extends BaseEntity {

	private static final long serialVersionUID = -6886632712563150379L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TEST_ID")
	private Test test;

	private Date startTime;

	// private long interval;

	private Date endTime;

	private long duration;

	private int processNum;

	private int threadNum;

	private int agentNum;

	// private List<String> agents;

	private int tests;

	private int errors;

	private Double meanTestTime;

	private Double testTimeStandardDeviation;

	private Double tps;

	private Double peakTps;

	// other...

}
