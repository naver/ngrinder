package org.ngrinder.common.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.ngrinder.common.util.PathUtils.getSubPath;


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

	@Test
	public void testGetSubPath() {
		String basePath = "/Users/user/dev/intellij-workspace/ngrinder-develop/";
		String path = "/Users/user/dev/intellij-workspace/ngrinder-develop/resources/resources.txt";

		assertThat(getSubPath(basePath, path)).isEqualTo("resources/resources.txt");
	}
}
