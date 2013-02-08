package org.ngrinder.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class UnitUtilTest {
	@Test
	public void testUnitUtil() {
		assertThat(UnitUtil.byteCountToDisplaySize(1024 * 1024 + 2), is("1.0MB"));
		assertThat(UnitUtil.byteCountToDisplaySize(1024 * 1024 + (1024 * 1024 / 10)), is("1.1MB"));
		assertThat(UnitUtil.byteCountToDisplaySize(1024 * 1024 * 1024 + 2), is("1.0GB"));
		assertThat(UnitUtil.byteCountToDisplaySize(1023), is("1023B"));
		assertThat(UnitUtil.byteCountToDisplaySize(1024), is("1.0KB"));
	}
}
