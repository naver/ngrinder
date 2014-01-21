package org.ngrinder.common.util;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;

public class PropertiesKeyMapperTest {
	@Test
	public void testMapperCreation() {
		PropertiesKeyMapper propertiesKeyMapper = PropertiesKeyMapper.create("agent-properties.map");
		Set<String> allKeys = propertiesKeyMapper.getAllKeys();
		List<String> all = newArrayList();
		all.addAll(allKeys);
		Collections.sort(all);
		for (String each : all) {
			System.out.println("public static final String PROP_" + each.toUpperCase().replace(".", "_") + " = \"" + each + "\";");
		}
	}

	@Test
	public void testEmptyKeyMapTest() {
		PropertiesKeyMapper propertiesKeyMapper = PropertiesKeyMapper.create("agent-properties.map");
		assertThat(propertiesKeyMapper.getKeys("agent.host_id")).isNotEmpty();
		assertThat(propertiesKeyMapper.getDefaultValue("agent.host_id")).isNull();
		assertThat(propertiesKeyMapper.getKeys("agent.java_opt")).isNotNull().isNotEmpty();
		assertThat(propertiesKeyMapper.getDefaultValue("agent.java_opt")).isNull();
	}
}
