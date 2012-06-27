package com.nhncorp.ngrinder.model;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.nhncorp.ngrinder.core.project.ProjectRepository;

@ContextConfiguration("classpath:applicationContext.xml")
public class ProjectTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	public void testProjectInsertion() {
		ProjectBean project = new ProjectBean();
		project.setName("My Test");
		project.setDescription("HWLLOWEWE");
		project.setLastModifiedDate(new Date());
		project.setLastModifiedUser("nb11323");
		project.setCreateDate(new Date());
		project.setCreateUser("nb113823");
		projectRepository.save(project);
		assertThat(project.getId(), notNullValue());
		ProjectBean findOneByName = projectRepository.findOneByName("My Test");
		assertThat(findOneByName, notNullValue());

	}
}
