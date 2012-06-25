package com.nhncorp.ngrinder.test.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nhncorp.ngrinder.test.dao.TestDao;
import com.nhncorp.ngrinder.test.model.Test;
import com.nhncorp.ngrinder.test.service.TestService;

@Service
public class TestServiceImpl implements TestService {

	@Autowired
	private TestDao testDao;

	@Override
	public Page<Test> getTests(Pageable pageable) {
		Page<Test> tests = testDao.getScripts(pageable);
		return tests;
	}

	@Override
	public Test getTest(long id) {
		Test test = testDao.findOne(id);
		return test;
	}

	@Override
	public void saveTest(Test test) {
		testDao.save(test);
	}

	@Override
	public void deleteTest(long id) {
		testDao.delete(id);
	}

}
