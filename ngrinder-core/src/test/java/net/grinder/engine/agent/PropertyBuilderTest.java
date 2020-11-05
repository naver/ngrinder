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
package net.grinder.engine.agent;

import net.grinder.common.GrinderProperties;
import net.grinder.util.Directory;
import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.NetworkUtils;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ngrinder.common.constants.GrinderConstants.GRINDER_SECURITY_LEVEL_NORMAL;

public class PropertyBuilderTest {
	@Test
	public void testPropertyBuilder() throws DirectoryException {
		System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator
				+ new File("./native_lib").getAbsolutePath());

		PropertyBuilder propertyBuilder = createPropertyBuilder("www.sample.com,:127.0.0.1");
		assertTrue(propertyBuilder.rebaseHostString("www.sample.com,:127.0.0.1")
			.matches("www.sample.com:.*,:127.0.0.1"));
		assertThat(propertyBuilder.rebaseHostString("www.sample.com:74.125.128.99"),
				is("www.sample.com:74.125.128.99"));
		assertThat(propertyBuilder.rebaseHostString(":127.0.0.1"), is(":127.0.0.1"));
	}

	@Test
	public void testDnsServerResolver() throws DirectoryException {
		PropertyBuilder propertyBuilder = createPropertyBuilder("www.sample.com,:127.0.0.1");
		StringBuilder builder = new StringBuilder();
		propertyBuilder.addDnsIP(builder);
		assertThat(builder.toString(), containsString("ngrinder.dns.ip="));
		assertThat(builder.length(), greaterThan(20));
	}

	@Test
	public void testPropertyBuilderMemSize() throws DirectoryException {
		System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator
				+ new File("./native_lib").getAbsolutePath());

		PropertyBuilder propertyBuilder = createPropertyBuilder("www.sample.com,:127.0.0.1");
		propertyBuilder.addProperties("grinder.processes", "10");
		String buildJVMArgument = propertyBuilder.buildJVMArgument();
		assertThat(buildJVMArgument, containsString("-Xmx"));
	}

	public PropertyBuilder createPropertyBuilder(String hostString) throws DirectoryException {
		Directory directory = new Directory(new File("."));
		GrinderProperties grinderProperties = new GrinderProperties();

		return new PropertyBuilder(grinderProperties, directory, true, GRINDER_SECURITY_LEVEL_NORMAL, hostString, NetworkUtils.getLocalHostName());
	}
}
