package org.ngrinder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * This class is used as base class for test case
 * 
 * @author Mavlarn
 * 
 */
@ContextConfiguration({ "classpath:applicationContext.xml" })
public class NGrinderIocTestBase extends AbstractJUnit4SpringContextTests {

	private static String fileStorePath;
	
	private static String classPath = NGrinderIocTestBase.class.getResource("/").getPath();

	@BeforeClass
	public static void init() throws IOException {
		fileStorePath = "" + InetAddress.getLocalHost().getHostName() + "-file-store";
		String currentPath = fileStorePath + File.separator + "current";
		String propFilePath = currentPath + File.separator + "grinder.properties";
		File propertyFile = new File(propFilePath);

		String scriptFilePath = currentPath + File.separator + "jartest.py";
		File scriptFile = new File(scriptFilePath);

		File srcPropertyFile = new File(classPath + "grinder-standalone.properties");
		File srcScriptFile = new File(classPath + "jartest.py");

		FileUtils.copyFile(srcPropertyFile, propertyFile);
		FileUtils.copyFile(srcScriptFile, scriptFile);
	}
 
}
