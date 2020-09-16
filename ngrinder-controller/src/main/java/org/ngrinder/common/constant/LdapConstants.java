/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.common.constant;

public interface LdapConstants {
	String PROP_LDAP_ENABLED = "ldap.enabled";
	String PROP_LDAP_SERVER = "ldap.server";
	String PROP_LDAP_PORT = "ldap.port";
	String PROP_LDAP_BASE_DN = "ldap.base.dn";
	String PROP_LDAP_USER_SEARCH_BASE = "ldap.user.search.base";
	String PROP_LDAP_USER_SEARCH_FILTER = "ldap.user.search.filter";
	String PROP_LDAP_USER_DISPLAY_NAME = "ldap.user.display_name";
	String PROP_LDAP_USER_EMAIL = "ldap.user.email";
	String PROP_LDAP_MANAGER_DN = "ldap.manager.dn";
	String PROP_LDAP_MANAGER_PASSWORD = "ldap.manager.password";
	String PROP_LDAP_CONNECT_TIMEOUT = "ldap.connect.timeout";
	String PROP_LDAP_RESPONSE_TIMEOUT = "ldap.response.timeout";
}
