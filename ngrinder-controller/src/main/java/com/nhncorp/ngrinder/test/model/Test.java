package com.nhncorp.ngrinder.test.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nhncorp.ngrinder.core.model.BaseEntity;
import com.nhncorp.ngrinder.script.model.Script;

/**
 * test entity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Entity
@Table(name = "TB_TEST")
public class Test extends BaseEntity {

	private static final long serialVersionUID = 5412910510490091114L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SCRIPT_ID")
	private Script script;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TEST_PARAMS_ID")
	private TestParams params;

	private String description;

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public TestParams getParams() {
		return params;
	}

	public void setParams(TestParams params) {
		this.params = params;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
