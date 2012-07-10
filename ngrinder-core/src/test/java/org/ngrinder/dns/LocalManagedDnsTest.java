package org.ngrinder.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LocalManagedDnsTest {

	@Before
	public void before() {
		// enable local DNS
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns," + LocalManagedDnsDescriptor.DNS_PROVIDER_NAME);
		System.setProperty("sun.net.spi.nameservice.provider.2", "dns,sun");

		// disable DNS cache.
		// Cache should be enabled later..
		Security.setProperty("networkaddress.cache.ttl", "0");

	}

	@Test
	@Ignore("Only this test is avalible when we provide JVM options on test runner")
	public void testCustomDNSLookup() throws UnknownHostException {
		performLookup("www.google.com");
		performLookup("www.google2.com");

	}

	public static void performLookup(String hostName) throws UnknownHostException {
		InetAddress[] addrs = InetAddress.getAllByName(hostName);
		for (int j = 0; j < addrs.length; j++) {
			System.out.println(addrs[j].toString());
		}
	}

	public static void test1(String[] args) throws UnknownHostException {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				InetAddress[] addrs = InetAddress.getAllByName(args[i]);
				for (int j = 0; j < addrs.length; j++) {
					addrs[i].getHostName();
					System.out.println(addrs[j].toString());
				}
			}
		}

	}

}
