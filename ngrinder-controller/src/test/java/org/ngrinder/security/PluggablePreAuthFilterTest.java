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

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.PassThroughFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import freemarker.ext.servlet.FreemarkerServlet;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class PluggablePreAuthFilterTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private PluginManager pluginManager;
	
	@Test
	public void test() throws IOException, ServletException {
		PluggablePreAuthFilter filter = new PluggablePreAuthFilter();
		ReflectionTestUtils.setField(filter, "pluginManager", pluginManager);
		
		filter.init();
		filter.onPluginEnabled(null);
		filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
				new PassThroughFilterChain(new FreemarkerServlet()));
		filter.onPluginDisabled(null);
	}
}
