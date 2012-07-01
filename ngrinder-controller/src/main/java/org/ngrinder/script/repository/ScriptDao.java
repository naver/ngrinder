package org.ngrinder.script.repository;

import java.util.List;

import org.ngrinder.script.model.Script;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;


public interface ScriptDao extends Repository<Script, Long> {

	List<Script> findAll();

	// @Query("from Script s where s.fileName like '%:searchStr%'"
	// +
	// " or s.lastModifiedUser like '%:searchStr%' or s.tags like '%:searchStr%'")
	// Page<Script> getScripts(String searchStr, Pageable pageable);

	/**
	 * @param share
	 *            get the shared data
	 * @param searchStr
	 *            keywords of file name/tags/modify user name
	 * @param pageable
	 * @return
	 */
	Page<Script> getScripts(boolean share, String searchStr, Pageable pageable);

	Script findOne(long id);

	void save(Script script);

	void delete(long id);

}
