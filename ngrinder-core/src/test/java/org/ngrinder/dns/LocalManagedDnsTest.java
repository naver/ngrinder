package org.ngrinder.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * To test the custom DNS, should to add vm arguments as below:
	-Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDns
	-Dngrinder.etc.hosts=10.0.0.1:www.google.com,10.0.0.2:www.google2.com
 *
 * @author Mavlarn
 * @since
 */
public class LocalManagedDnsTest {
	
	LocalManagedDns localNS = new LocalManagedDns();

	public void beforeCustomDNS() {
		// enable local DNS
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns," + LocalManagedDnsDescriptor.DNS_PROVIDER_NAME);
		System.setProperty("sun.net.spi.nameservice.provider.2", "dns,sun");

		// disable DNS cache.
		// Cache should be enabled later..
		Security.setProperty("networkaddress.cache.ttl", "0");
	}
	
	public void cleanCustomDNS() {
		// enable local DNS
		System.clearProperty("sun.net.spi.nameservice.provider.1");
		System.clearProperty("sun.net.spi.nameservice.provider.2");

		// enable DNS cache.
		Security.setProperty("networkaddress.cache.ttl", "1");
	}

	@Test
	@Ignore("Only this test is available when we provide JVM options on test runner")
	public void testCustomDNSLookup() throws UnknownHostException {
		System.out.println("Test with custom nameservice provider.");
		beforeCustomDNS();
		performLookup("www.google.com");
		performLookup("www.google2.com");
		performLookup("www.baidu.com");
		performLookup("www.naver.com");
		
		NameStore.getInstance().put("mydomain.com", "10.10.10.10");
		//performLookupLocal("mydomain.com");
		performLookup("mydomain.com");
		cleanCustomDNS();
	}

	@Test
	@Ignore("Only this test is available when we provide JVM options on test runner")
	public void testOriginalDNSLookup() throws UnknownHostException {
		System.out.println("Clear nameservice provider from system properties and test.");
		cleanCustomDNS();
		performLookup("www.google.com");
		performLookup("www.google2.com");
		performLookup("www.baidu.com");
		performLookup("www.naver.com");
		
		NameStore.getInstance().put("mydomain.com", "10.10.10.10");
		//performLookupLocal("mydomain.com");
		performLookup("mydomain.com");

	}

	@Test
	public void testLocalDNSLookup() throws UnknownHostException {
		System.out.println("test LocalDNSLookup.....");
		cleanCustomDNS();
		performLookupLocal("www.google.com");
		performLookupLocal("www.google2.com");
		performLookup("www.baidu.com");
		performLookup("www.naver.com");
		
		NameStore.getInstance().put("mydomain.com", "10.10.10.10");
		//performLookupLocal("mydomain.com");
		performLookupLocal("mydomain.com");

	}

	public static void performLookup(String hostName) throws UnknownHostException {
		InetAddress[] addrs = InetAddress.getAllByName(hostName);
		for (int j = 0; j < addrs.length; j++) {
			System.out.println(addrs[j].toString());
		}
	}

	public void performLookupLocal(String hostName) throws UnknownHostException {
		InetAddress[] addrs = localNS.lookupAllHostAddr(hostName);
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
