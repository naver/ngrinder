package com.nhncorp.ngrinder.core.user;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhncorp.ngrinder.core.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	public List<User> findAll(Specification<User> spec);
}
