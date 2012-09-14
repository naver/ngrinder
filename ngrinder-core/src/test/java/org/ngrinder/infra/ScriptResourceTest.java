package org.ngrinder.infra;

import static net.grinder.util.ClassLoaderUtilities.allResourceLines;

import java.io.IOException;
import java.util.List;

import net.grinder.scriptengine.ScriptEngineService;

import org.junit.Test;

public class ScriptResourceTest {

	@Test
	public void test() throws IOException {
		final List<String> implementationNames;
		implementationNames = allResourceLines(getClass().getClassLoader(), ScriptEngineService.RESOURCE_NAME);
		System.out.println(implementationNames);
	}
}
