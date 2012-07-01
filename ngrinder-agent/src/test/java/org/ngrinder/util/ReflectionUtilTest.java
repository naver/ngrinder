package org.ngrinder.util;

import org.junit.Assert;
import org.junit.Test;
import org.ngrinder.util.ReflectionUtil;

public class ReflectionUtilTest {
	@Test
	public void testGetFieldValue() {
		String name = "TestObj001";
		TestObj to = new TestObj(name);
		String name2 = (String) ReflectionUtil.getFieldValue(to, "name");
		Assert.assertEquals(name, name2);

		try {
			ReflectionUtil.getFieldValue(null, "name");
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}
		try {
			ReflectionUtil.getFieldValue(to, null);
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testSetFieldValue() {
		String name = "TestObj001";
		TestObj to = new TestObj(name);
		ReflectionUtil.setFieldValue(to, "name", name);
		Assert.assertEquals(name, to.getName());

		try {
			ReflectionUtil.setFieldValue(null, "name", name);
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}
		try {
			ReflectionUtil.setFieldValue(to, null, name);
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}
	}

	private class TestObj {
		public TestObj(String name) {
			this.name = name;
		}

		private String name;

		public String getName() {
			return name;
		}
	}
}
