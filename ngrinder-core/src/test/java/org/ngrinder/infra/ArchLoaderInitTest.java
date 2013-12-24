package org.ngrinder.infra;

import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class ArchLoaderInitTest {
	@Test
	public void testArchLoader() throws ArchNotSupportedException, ArchLoaderException, UnsupportedEncodingException {
		AgentConfig agentConfig = new AgentConfig.NullAgentConfig(1).init();
		System.out.println(System.getProperty("java.library.path"));
		ArchLoaderInit archLoaderInit = new ArchLoaderInit();
		archLoaderInit.init(agentConfig.getHome().getNativeDirectory());
		Sigar sigar = new Sigar();
		sigar.getNativeLibrary();
		final String name = Charset.defaultCharset().name();
		System.out.println(name);
		System.out.println(URLDecoder.decode("D:\\nGrinder%20%ec%9a%b4%ec%98%81\\nGrinder%203" +
				".3%20Release%20Package\\ngrinder-monitor\\lib\\sigar-native-1" +
				".0.jar", name));
		;
	}
}
