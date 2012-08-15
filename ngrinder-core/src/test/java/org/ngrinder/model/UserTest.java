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
package org.ngrinder.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class UserTest {


	@Test
	public void testValidate() {
		User user1 = new User("Uid1", "name1", "pwd1", Role.USER);
		user1.setEmail("aa@bb.com");
		assertTrue(user1.validate());

		User user2 = new User();
		user2 = new User("Uid1", null, "pwd1", Role.USER);
		assertTrue(!user2.validate());

		user2 = new User("Uid1", "name", "pwd1", null);
		assertTrue(!user2.validate());
		
	}
	
	@Test
	public void testEqualsObject() {
		User user1 = new User("Uid1", "name1", "pwd1", Role.USER);		
		User user2 = new User("Uid1", "name2", "pwd2", Role.USER);
		assertThat(user1, is(user2));
		assertThat(user1.hashCode(), is(user2.hashCode()));

		user2.setUserId("Uid2");
		assertThat(user1, not(user2));
		assertThat(user1.hashCode(), not(user2.hashCode()));

		user2.setUserId(null);
		assertThat(user1, not(user2));
		assertThat(user1.hashCode(), not(user2.hashCode()));
		
		assertTrue(!user1.equals(null));
	}

}
