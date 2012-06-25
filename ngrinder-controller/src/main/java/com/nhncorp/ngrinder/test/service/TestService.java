package com.nhncorp.ngrinder.test.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nhncorp.ngrinder.test.model.Test;

public interface TestService {

	Page<Test> getTests(Pageable pageable);

	Test getTest(long id);

	void saveTest(Test test);

	void deleteTest(long id);

}
