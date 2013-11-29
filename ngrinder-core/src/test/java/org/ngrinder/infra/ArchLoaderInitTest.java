package org.ngrinder.infra;

import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.junit.Test;

public class ArchLoaderInitTest {
	@Test
	public void testArchLoader() throws ArchNotSupportedException, ArchLoaderException {
		AgentConfig agentConfig = new AgentConfig.NullAgentConfig(1).init();
		System.out.println(System.getProperty("java.library.path"));
		ArchLoaderInit archLoaderInit = new ArchLoaderInit();
		archLoaderInit.init(agentConfig.getHome().getNativeDirectory());
		Sigar sigar = new Sigar();
		sigar.getNativeLibrary();
	}
}
