package org.ngrinder.perftest.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.junit.Test;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.python.google.common.collect.Lists;

import java.lang.reflect.Type;
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
		Gson gson = new Gson();
		String json = gson.toJson(lists);
		ArrayList<SystemDataModel> fromJson = gson.fromJson(json, getTypeToken());
		System.out.println(fromJson.get(0).getClass());
	}

	private Type getTypeToken() {
		return new TypeToken<ArrayList<SystemDataModel>>() {
			private static final long serialVersionUID = 1L;
		}.getType();
	}
}
