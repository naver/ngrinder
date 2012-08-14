package org.ngrinder.perftest.service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class PerfTestVUserCalcTest {

	@Test
	public void testPerfTestScript() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
		engine.put("A", 10);
		engine.put("B", 4);

		System.out.println(engine.eval(" [(A / 2), (A + 3)];") );
	}
}
