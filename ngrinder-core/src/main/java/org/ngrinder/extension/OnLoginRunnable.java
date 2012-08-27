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
package org.ngrinder.extension;

import org.ngrinder.model.User;

/**
 * Plugin extension for user authentication.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnLoginRunnable {
	/**
	 * Load user by userId
	 * 
	 * @param userId
	 * @return User instance
	 */
	public User loadUser(String userId);

	/**
	 * Validate user by userId and password.
	 * 
	 * Against password can be provided by plugin. In such case encPass, encoder, salt might be null.
	 * 
	 * @param userId
	 *            user providing id
	 * @param password
	 *            user providing password
	 * @param encPass
	 *            encrypted password
	 * @param encoder
	 *            encoder which encrypts password
	 * @param salt
	 *            salt of encoding
	 * @return true is validated
	 */
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt);

	/**
	 * Save user in plugin.<br/>
	 * This method is only necessary to implement if there is need to save the user in the plugin. Generally dummy
	 * implementation is enough.
	 * 
	 * @param user
	 *            user to be saved.
	 */
	public void saveUser(User user);
}
