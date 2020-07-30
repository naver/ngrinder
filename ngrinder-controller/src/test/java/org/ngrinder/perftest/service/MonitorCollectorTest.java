package org.ngrinder.perftest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.ngrinder.common.util.JsonUtils;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.python.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class MonitorCollectorTest {

	@Test
	public void testMonitor() {
		SystemDataModel systemDataModel = new SystemDataModel();
		systemDataModel.setCollectTime(100L);
		systemDataModel.setFreeMemory(20L);
		systemDataModel.setCpuUsedPercentage(20f);
		List<SystemDataModel> lists = Lists.newArrayList();
		lists.add(systemDataModel);
		lists.add(systemDataModel);

		String json = JsonUtils.serialize(lists);
		ArrayList<SystemDataModel> fromJson = JsonUtils.deserialize(json, new TypeReference<ArrayList<SystemDataModel>>() {
		});
		System.out.println(fromJson.get(0).getClass());
	}
}
