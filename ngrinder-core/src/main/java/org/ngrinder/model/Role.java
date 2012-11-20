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


/**
 * Role of the User.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum Role {
	/**
	 * General user role who can create performance test entry.
	 */
	USER("U", "General") {

	},
	/**
	 * Admin user role who can monitors tests.
	 */
	ADMIN("A", "Administrator") {
		public boolean canGetAllTests() {
			return true;
		}
	},
	/**
	 * Super user role who can set system settings and manage user account.
	 */
	SUPER_USER("S", "Super") {
		public boolean canGetAllTests() {
			return true;
		}
	},
	/**
	 * System user role. This is for the automatic batch
	 */
	SYSTEM_USER("SYSTEM", "System User") {

	};

	private final String shortName;

	private final String fullName;

	/**
	 * Constructor.
	 * 
	 * @param shortName
	 *            short name of role... usually 1 sing char
	 * @param fullName
	 *            full name of role
	 */
	Role(String shortName, String fullName) {
		this.shortName = shortName;
		this.fullName = fullName;
	}

	/**
	 * check whether a user can get tests of others.
	 * @return true if can
	 */
	public boolean canGetAllTests() {
		return false;
	}

	public boolean canModifyTestOfOther() {
		return false;
	}

	public boolean canDeleteTestOfOther() {
		return false;
	}

	public boolean canStopTestOfOther() {
		return false;
	}

	public boolean canCheckScriptOfOther() {
		return false;
	}

	public boolean canValidateScriptOfOther() {
		return false;
	}
	
	
	/**
	 * Get the short name.
	 * 
	 * @return short name
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Get full name.
	 * 
	 * @return full name
	 */
	public String getFullName() {
		return fullName;
	}
}