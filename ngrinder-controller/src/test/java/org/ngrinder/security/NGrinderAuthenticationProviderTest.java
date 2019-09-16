/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.security;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Class description.
 *
 * @author Mavlarn
 */
public class NGrinderAuthenticationProviderTest extends AbstractNGrinderTransactionalTest {

    @Autowired
    private NGrinderAuthenticationProvider provider;

    @Autowired
    private NGrinderUserDetailsService userDetailService;

    @Test
    public void testAdditionalAuthenticationChecks() {
        UserDetails user = userDetailService.loadUserByUsername(getTestUser().getUserId());

        //remove authentication temporally
        Authentication oriAuth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextImpl context = new SecurityContextImpl();
        SecurityContextHolder.setContext(context);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin", null);

		try {
			provider.additionalAuthenticationChecks(user, token);
			fail();
		} catch (BadCredentialsException ignored) {
		}

        token = new UsernamePasswordAuthenticationToken("TEST_USER", "123");
        provider.additionalAuthenticationChecks(user, token);

        context.setAuthentication(oriAuth);
    }

	@Test
	public void testAddNewUserIntoLocal() {
		User user = User.createNew();
		user.setEmail("aaa@hello.com");
		user.setUserId("new_user");
		user.setPassword("new_user");
		user.setUserName("new_user");
		provider.addNewUserIntoLocal(new SecuredUser(user, null));
		UserDetails userDetails = userDetailService.loadUserByUsername(user.getUserId());
		assertThat(userDetails.getUsername(), equalTo(user.getUserId()));
	}
}
