package com.nhncorp.ngrinder.controller;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.nhncorp.ngrinder.NGrinderIocTestBase;
import com.nhncorp.ngrinder.bo.HostsBO;
import com.nhncorp.ngrinder.util.HttpUtils;

public class HostControllerTest extends NGrinderIocTestBase {
	private HostController hostController;

	@Autowired
	private HostsBO hostsBO;

	@Before
	public void initController() {
		if (hostController == null) {
			hostController = new HostController();
			ReflectionTestUtils.setField(hostController, "hostsBO", hostsBO);
		}
	}

	@Test
	public void testUpdateHostsFile() {
		String result = hostController.updateHostsFile("12345");
		@SuppressWarnings("unchecked")
		Map<String, Object> rtnMap = HttpUtils.getAsMap(result);
		Assert.assertTrue(rtnMap != null && rtnMap.size() > 0);

		this.testRecoverHostsFile();
	}

	@Test
	public void testRecoverHostsFile() {
		String result = hostController.recoverHostsFile();
		@SuppressWarnings("unchecked")
		Map<String, Object> rtnMap = HttpUtils.getAsMap(result);
		Assert.assertTrue(rtnMap != null && rtnMap.size() > 0);
	}

	@Test
	public void testGetAgentDate() {
		String result = hostController.getAgentDate();
		Assert.assertNotNull(result);
	}

}
