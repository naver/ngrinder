package org.ngrinder.infra;

import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: junoyoon
 * Date: 13. 11. 25
 * Time: 오후 2:00
 * To change this template use File | Settings | File Templates.
 */
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
