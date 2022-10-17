package org.ngrinder.common.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ngrinder.common.util.StringUtils.replaceLast;

public class StringUtilsTest {

	@Test
	public void testReplaceLast() {
		String str1 = "ngrinder-core-3.5.1-p1.jar";
		assertThat(replaceLast(str1, "-p[1-9]", ""), is("ngrinder-core-3.5.1.jar"));

		String str2 = "ngrin-p2der-c-p3ore-3.5.1-p1.jar";
		assertThat(replaceLast(str2, "-p[1-9]", ""), is("ngrin-p2der-c-p3ore-3.5.1.jar"));

		String str3 = "ngr-pginder-core-3.5.1-p0.jar";
		assertThat(replaceLast(str3, "-p[1-9]", ""), is("ngr-pginder-core-3.5.1-p0.jar"));
	}
}
