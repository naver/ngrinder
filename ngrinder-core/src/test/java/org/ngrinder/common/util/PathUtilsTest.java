package org.ngrinder.common.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class PathUtilsTest {
	@Test
	public void testJoin() throws Exception {
		assertThat(PathUtils.join("/hello/world", "wow")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("/hello/world", "wow/")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("/hello/world//", "wow/")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("/hello/world////", "//wow/")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("/////hello/world////", "//wow/")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("hello/world////", "//wow/")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("hello/world////", "//wow/")).isEqualTo("hello/world/wow");
		assertThat(PathUtils.join("/hello/world////", "//wow////")).isEqualTo("hello/world/wow");
	}
}
