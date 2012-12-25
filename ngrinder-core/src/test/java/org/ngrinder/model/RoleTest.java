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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class RoleTest {
	
	@Test
	public void testHasPermission() {
		Role user = Role.USER;
		assertFalse(user.hasPermission(Permission.GET_ALL_TESTS));
		assertFalse(user.hasPermission(Permission.DELETE_TEST_OFOTHER));
		assertFalse(user.hasPermission(Permission.CHECK_SCRIPT_OFOTHER));
		assertFalse(user.hasPermission(Permission.VALIDATE_SCRIPT_OFOTHER));
		assertFalse(user.hasPermission(Permission.STOP_TEST_OFOTHER));
		assertFalse(user.hasPermission(Permission.SWITCH_TO_ANYONE));

		Role admin = Role.ADMIN;
		assertTrue(admin.hasPermission(Permission.GET_ALL_TESTS));
		assertTrue(admin.hasPermission(Permission.DELETE_TEST_OFOTHER));
		assertTrue(admin.hasPermission(Permission.CHECK_SCRIPT_OFOTHER));
		assertTrue(admin.hasPermission(Permission.VALIDATE_SCRIPT_OFOTHER));
		assertTrue(admin.hasPermission(Permission.STOP_TEST_OFOTHER));
		assertTrue(admin.hasPermission(Permission.SWITCH_TO_ANYONE));

		Role superUser = Role.SUPER_USER;
		assertTrue(superUser.hasPermission(Permission.GET_ALL_TESTS));
		assertFalse(superUser.hasPermission(Permission.DELETE_TEST_OFOTHER));
		assertTrue(superUser.hasPermission(Permission.CHECK_SCRIPT_OFOTHER));
		assertTrue(superUser.hasPermission(Permission.VALIDATE_SCRIPT_OFOTHER));
		assertFalse(superUser.hasPermission(Permission.STOP_TEST_OFOTHER));
		assertTrue(superUser.hasPermission(Permission.SWITCH_TO_ANYONE));
	}

}
