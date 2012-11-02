package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class NumberFormatTest {

	@Test
	public void testNumberFormat() {
		PerfTestService perfTestService = new PerfTestService();
		Map<String, String> map = new HashMap<String, String>();
		map.put("HELLO", "100,000");
		assertThat(perfTestService.parseDoubleWithSafety(map, "HELLO", 3d), is(100000D));
	}
}
