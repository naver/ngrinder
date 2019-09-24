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
package org.ngrinder.script.repository;

import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

@Profile("unit-test")
@Component
public class MockFileEntityRepository extends FileEntryRepository {

	private File userRepoDir;

	public MockFileEntityRepository(Config config, UserContext userContext) {
		super(config, userContext);
	}

	@Override
	public File getUserRepoDirectory(User user) {
		return userRepoDir;
	}

	public void setUserRepository(File userRepository) {
		this.userRepoDir = userRepository;
	}
}
