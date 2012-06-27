package com.nhncorp.ngrinder.core.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhncorp.ngrinder.model.ProjectBean;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectBean, Long> {
	ProjectBean findOneByName(String name);
}
