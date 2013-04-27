package org.ngrinder.common.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class MoveDirectoryTest {
	
	public void testDirectoryMode() throws IOException {
		FileUtils.moveToDirectory(new File("d:/ee/result1"), new File("d:/ee/ee"), true);
	}
}
