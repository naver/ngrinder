package org.ngrinder.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ngrinder.MockControllerServer;
import org.ngrinder.util.HttpUtils;


public class HttpUtilsTest {
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	@Test
	public void testExecute() {
		MockControllerServer server = new MockControllerServer(1234);
		server.start();
		String result = null;
		try {
			result = HttpUtils.execute("127.0.0.1", 1234, "1", "2", "3", "4");
		} catch (Exception e) {
			Assert.assertFalse(true);
		}
		Assert.assertNotNull(result);
		server.stop();

		try {
			result = HttpUtils.execute("1.1.1.1", 1234, "1", "2", "3", new HashMap() {
				{
					this.put("a", "b");
					this.put("a", "b");
				}
			});
			Assert.assertFalse(true);
		} catch (Exception e) {
			Assert.assertFalse(false);
		}
	}

	@Test
	public void testGetAsMap() {
		@SuppressWarnings("rawtypes")
		Map result = HttpUtils.getAsMap("{\"name\":\"lucy\"}");
		Assert.assertNotNull(result);
	}
}
