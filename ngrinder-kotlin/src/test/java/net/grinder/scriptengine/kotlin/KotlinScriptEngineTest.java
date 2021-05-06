package net.grinder.scriptengine.kotlin;

import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import org.junit.Test;

import java.io.File;

public class KotlinScriptEngineTest {
	@Test
	public void testRunKotlinScript() throws EngineException {
		String filePath = "src/test/resources/TestRunner.kts";
		File file = new File(filePath);

		KotlinScriptEngine engine = new KotlinScriptEngine(new ScriptLocation(file.getAbsoluteFile()));
		KotlinScriptEngine.KotlinWorkerRunnable worker = (KotlinScriptEngine.KotlinWorkerRunnable) engine.createWorkerRunnable();
		worker.run();
		worker.shutdown();
	}
}
