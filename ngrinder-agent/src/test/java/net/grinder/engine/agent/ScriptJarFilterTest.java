package net.grinder.engine.agent;

import java.io.File;

import net.grinder.util.Directory;
import net.grinder.util.Directory.DirectoryException;

import org.junit.Assert;
import org.junit.Test;

public class ScriptJarFilterTest {
	
	@Test
	public void testListJars () throws DirectoryException {
		
		Directory currentDir = new Directory(new File("src/test/resources/jars"));
		File[] jars = currentDir.getFile().listFiles(new WorkerProcessCommandLine.ScriptJarFilter());
		Assert.assertTrue(jars.length == 3);
		for (File file : jars) {
			Assert.assertTrue(file.getName().endsWith(".jar"));
			Assert.assertTrue(!file.getName().endsWith(".tmp"));
		}
	}

}
