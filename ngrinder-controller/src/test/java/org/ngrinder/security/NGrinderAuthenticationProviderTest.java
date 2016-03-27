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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

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

    @Autowired
    private ShaPasswordEncoder passwordEncoder;

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
            assertTrue(false);
        } catch (BadCredentialsException e) {
            assertTrue(true);
        }

        token = new UsernamePasswordAuthenticationToken("TEST_USER", "123");
        provider.additionalAuthenticationChecks(user, token);

        context.setAuthentication(oriAuth);
    }

    @Test
    public void testSetPasswordEncoder() {
        PlaintextPasswordEncoder plaintextPasswordEncoder = new PlaintextPasswordEncoder();
        provider.setPasswordEncoder(plaintextPasswordEncoder);

        org.springframework.security.crypto.password.PasswordEncoder enc2 = new StandardPasswordEncoder();
        provider.setPasswordEncoder(enc2);

        provider.setPasswordEncoder(passwordEncoder);
    }

    @Test
    public void testAddNewUserIntoLocal() {
        SecuredUser secUser = new SecuredUser(getTestUser(), null);
        provider.addNewUserIntoLocal(secUser);
        assertThat(secUser.getUser(), is(getTestUser()));

        User tmpUser = new User("tmpUserId", "tmpName", "123", "test.nhn.com", Role.USER);
        SecuredUser tmpSecUser = new SecuredUser(tmpUser, null);
        provider.addNewUserIntoLocal(tmpSecUser);
        assertThat(tmpSecUser.getUser(), is(tmpUser));
    }
}
