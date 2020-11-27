package org.ngrinder.common.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

public class JsonUtilsTest {
	private static class NumberModel {
		public float floatNaN = Float.NaN;
		public double doubleNaN = Double.NaN;
		public int normalInt = 3;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNaNSerializeAsNull() {
		String jsonString = JsonUtils.serialize(new NumberModel());
		Map<String, Object> map = JsonUtils.deserialize(jsonString, HashMap.class);
		assertNull(map.get("floatNaN"));
		assertNull(map.get("doubleNaN"));
		assertThat(map.get("normalInt"), is(3));
	}
}
