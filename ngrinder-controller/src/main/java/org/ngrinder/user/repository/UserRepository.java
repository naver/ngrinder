package org.ngrinder.user.repository;

import java.util.List;

import org.ngrinder.user.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	public List<User> findAll(Specification<User> spec);

}
