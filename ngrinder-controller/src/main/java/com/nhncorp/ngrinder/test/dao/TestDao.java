package com.nhncorp.ngrinder.test.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.nhncorp.ngrinder.test.model.Test;

public interface TestDao extends Repository<Test, Long> {
	List<Test> findAll();

	@Query("from Test t")
	Page<Test> getScripts(Pageable pageable);

	Test findOne(Long id);

	void save(Test test);

	void delete(Long id);

}
