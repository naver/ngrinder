package net.grinder.util;

import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class GrinderUtilTest {
	@Test
	public void testAny() {
		String[] array = new String[] { "hello1", "hello2", "hello3", "hello4", "hello5", "hello6", "hello7" };
		List<String> asList = new ArrayList<String>(Arrays.asList(array));
		for (int i = 0; i < 1000000; i++) {
			if (asList.isEmpty()) {
				break;
			}
			if (i > 10000) {
				fail();
			}
			String any = GrinderUtil.any(array);
			System.out.println(any);
			if (asList.contains(any)) {
				asList.remove(any);
			}
		}

		asList = new ArrayList<String>(Arrays.asList(array));
		for (int i = 0; i < 1000000; i++) {
			if (asList.isEmpty()) {
				break;
			}
			if (i > 10000) {
				fail();
			}
			String any = GrinderUtil.any(asList);
			System.out.println(any);
			if (asList.contains(any)) {
				asList.remove(any);
			}
		}

	}
}
