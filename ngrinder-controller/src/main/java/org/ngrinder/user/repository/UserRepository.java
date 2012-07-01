package org.ngrinder.user.repository;

import java.util.List;

import org.ngrinder.user.model.NGrinderUser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<NGrinderUser, Integer> {
	public List<NGrinderUser> findAll(Specification<NGrinderUser> spec);
}
