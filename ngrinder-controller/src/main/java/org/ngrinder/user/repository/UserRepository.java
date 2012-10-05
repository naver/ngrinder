/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.user.repository;

import java.util.List;

import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * User repository.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	/**
	 * Find all {@link User}s based on the given spec.
	 * 
	 * @param spec
	 *            spec
	 * @return found {@link User} list
	 */
	public List<User> findAll(Specification<User> spec);

	/**
	 * Find all {@link User}s for the given role.
	 * 
	 * @param role
	 *            role
	 * @return found {@link User} list
	 */
	public List<User> findAllByRole(Role role);

	/**
	 * Delete user which has the given userId.
	 * 
	 * @param userId
	 *            user id
	 */
	@Modifying
	@Query("delete from User u where u.userId = :userId")
	public void deleteByUserId(@Param("userId") String userId);

	/**
	 * Find one {@link User} by the given userId.
	 * 
	 * @param userId
	 *            user id
	 * @return found {@link User}. null if not found.
	 */
	public User findOneByUserId(String userId);

	/**
	 * Find one {@link User} by the given userName.
	 * 
	 * @param userName
	 *            user name
	 * @return found {@link User}. null if not found.
	 */
	public User findOneByUserName(String userName);

}
