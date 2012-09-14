package org.ngrinder;

import java.io.File;

import org.junit.Before;

/**
 * TestBase for sigar lib path
 * 
 * @author JunHo Yoon
 */
public class SigarTestBase {

	@Before
	public void setupSigarLibPath() {
		System.setProperty("java.library.path",
				System.getProperty("java.library.path") + File.pathSeparator + new File("./native_lib").getAbsolutePath());
	}
}
