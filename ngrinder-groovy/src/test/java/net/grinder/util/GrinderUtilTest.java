package net.grinder.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.fail;

public class GrinderUtilTest {
	@Test
	public void testAny() {
		String[] array = new String[]{"hello1", "hello2", "hello3", "hello4", "hello5", "hello6", "hello7"};
		List<String> asList = new ArrayList<String>(Arrays.asList(array));
		for (int i = 0; i < 1000000; i++) {
			if (asList.isEmpty()) {
				break;
			}
			if (i > 10000) {
				fail();
			}
			String any = GrinderUtils.any(array);
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
			String any = GrinderUtils.any(asList);
			if (asList.contains(any)) {
				asList.remove(any);
			}
		}

	}
}
