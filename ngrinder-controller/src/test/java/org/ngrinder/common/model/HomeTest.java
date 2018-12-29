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
package org.ngrinder.common.model;

import org.junit.Test;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.starter.NGrinderControllerStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link Home} Test Class
 * 
 * @author JunHo Yoon
 * 
 */
@ActiveProfiles("unit-test")
@SpringBootTest(classes = NGrinderControllerStarter.class)
public class HomeTest extends AbstractJUnit4SpringContextTests implements ControllerConstants {

	@Autowired
	private Config config;

	@Test
	public void testPerfTestFolderGeneration() {
		// Given
		Home home = config.getHome();
		// When
		File perfTestDirectory = home.getPerfTestDirectory("30001");
		// Then
		assertThat(perfTestDirectory.getName(), is("30001"));
		assertThat(perfTestDirectory.getParentFile().getName(), is("30000_30999"));
		assertThat(perfTestDirectory.exists(), is(true));
	}
}
