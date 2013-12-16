package org.ngrinder.common.util;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;

public class ControllerPropertiesKeyMapperTest {
	@Test
	public void testMapperCreation() {
		PropertiesKeyMapper propertiesKeyMapper = PropertiesKeyMapper.create("controller-properties.map");
		Set<String> allKeys = propertiesKeyMapper.getAllKeys();
		List<String> all = newArrayList();
		all.addAll(allKeys);
		Collections.sort(all);
		for (String each : all) {
			System.out.println("public static final String PROP_" + each.toUpperCase().replace(".", "_") + " = \"" + each + "\";");
		}
	}
}
