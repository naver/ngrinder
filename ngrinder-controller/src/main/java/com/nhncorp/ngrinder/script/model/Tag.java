package com.nhncorp.ngrinder.script.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.nhncorp.ngrinder.core.model.BaseEntity;

/**
 * tag eneity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Entity
@Table(name = "TAG")
public class Tag extends BaseEntity {

	private static final long serialVersionUID = -418147255758574079L;

	@ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
	private List<Script> scripts;

	@Column(name = "name", nullable = false)
	private String name;

	@Override
	public String toString() {
		return "Tag [name=" + name + "]";
	}

	public List<Script> getScripts() {
		return scripts;
	}

	public void setScripts(List<Script> scripts) {
		this.scripts = scripts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
