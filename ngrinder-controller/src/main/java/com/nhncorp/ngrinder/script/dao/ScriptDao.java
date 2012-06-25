package com.nhncorp.ngrinder.script.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.nhncorp.ngrinder.script.model.Script;

public interface ScriptDao extends Repository<Script, Long> {

	List<Script> findAll();

	@Query("from Script s where s.fileName like '%:searchStr%'"
			+ " or s.lastModifiedUser like '%:searchStr%' or s.tags like '%:searchStr%'")
	Page<Script> getScripts(String searchStr, Pageable pageable);

	Script findOne(long id);

	void save(Script script);

	void delete(long id);

}
