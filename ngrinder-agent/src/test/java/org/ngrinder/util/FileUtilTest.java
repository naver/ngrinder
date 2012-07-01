package org.ngrinder.util;

import junit.framework.Assert;

import org.junit.Test;
import org.ngrinder.util.FileUtil;


public class FileUtilTest {

	@Test
	public void testGetHostsFile() {
		Assert.assertEquals(FileUtil.getHostsFileName("linux"), "/etc/hosts");
		Assert.assertEquals(FileUtil.getHostsFileName("windows xp"), "C:\\WINDOWS\\system32\\drivers\\etc\\hosts");
	}

}
