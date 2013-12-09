package org.ngrinder.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import net.grinder.util.UnitUtils;

import org.junit.Test;

public class UnitUtilTest {
	@Test
	public void testUnitUtil() {
		assertThat(UnitUtils.byteCountToDisplaySize(1024 * 1024 + 2), is("1.0MB"));
		assertThat(UnitUtils.byteCountToDisplaySize(1024 * 1024 + (1024 * 1024 / 10)), is("1.1MB"));
		assertThat(UnitUtils.byteCountToDisplaySize(1024 * 1024 * 1024 + 2), is("1.0GB"));
		assertThat(UnitUtils.byteCountToDisplaySize(1023), is("1023B"));
		assertThat(UnitUtils.byteCountToDisplaySize(1024), is("1.0KB"));
	}
}
