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

import static org.ngrinder.common.util.TypeConvertUtils.cast;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

/**
 * User Switch Recognized Voter for SVN.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
@Component("userSwitchPermissionVoter")
public class UserSwitchPermissionVoter implements AccessDecisionVoter<FilterInvocation> {

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(FilterInvocation.class);
	}

	@Override
	public int vote(Authentication authentication, FilterInvocation filter, Collection<ConfigAttribute> attributes) {
		if ("anonymousUser".equals(authentication.getPrincipal())) {
			return ACCESS_DENIED;
		}
		if (!(authentication.getPrincipal() instanceof SecuredUser)) {
			return ACCESS_DENIED;
		}
		SecuredUser secureUser = cast(authentication.getPrincipal());
		User user = secureUser.getUser();
		if (user.getRole() == Role.ADMIN) {
			return ACCESS_GRANTED;
		}

		String realm = StringUtils.split(filter.getHttpRequest().getPathInfo(), '/')[0];
		if (secureUser.getUsername().equals(realm)) {
			return ACCESS_GRANTED;
		} else {
			List<User> owners = user.getOwners();
			for (User each : owners) {
				if (realm.equals(each.getUserId())) {
					return ACCESS_GRANTED;
				}
			}
		}
		return ACCESS_DENIED;
	}
}
