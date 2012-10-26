package net.grinder.engine.agent;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import net.grinder.common.GrinderProperties;
import net.grinder.util.Directory;
import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.NetworkUtil;

import org.junit.Test;

public class PropertyBuilderTest {
	@Test
	public void testPropertyBuilder() throws DirectoryException {
		PropertyBuilder createPropertyBuilder = createPropertyBuilder("www.sample.com,:127.0.0.1");
		assertThat(createPropertyBuilder.rebaseHostString("www.sample.com,:127.0.0.1"), is("www.sample.com:173.230.129.147,:127.0.0.1"));
		assertThat(createPropertyBuilder.rebaseHostString("www.sample.com:74.125.128.99"), is("www.sample.com:74.125.128.99"));
		assertThat(createPropertyBuilder.rebaseHostString("www.google.com").length(), greaterThan(40));
		assertThat(createPropertyBuilder.rebaseHostString(":127.0.0.1"), is(":127.0.0.1"));
	}

	public PropertyBuilder createPropertyBuilder(String hostString) throws DirectoryException {
		Directory directory = new Directory(new File("."));
		GrinderProperties property = new GrinderProperties();
		return new PropertyBuilder(property, directory, new File("."), true, hostString, NetworkUtil.getLocalHostName());
	}
}
