package org.ngrinder.dns;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

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

	/**
	 * This test can only run with vm option:
	 * -Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDns
	 * @throws UnknownHostException
	 */
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

		NameStore.getInstance().put("www.google.com", "10.0.0.1");
		NameStore.getInstance().put("www.google2.com", "10.0.0.2");
		
		InetAddress[] addrs = localNS.lookupAllHostAddr("www.google.com");
		boolean success = false;
		for (InetAddress inetAddress : addrs) {
			if (inetAddress.getHostAddress().equals("10.0.0.1")) {
				success = true;
			} else {
			}
		}
		assertTrue(success);
		
		performLookupLocal("www.google2.com");
		addrs = localNS.lookupAllHostAddr("www.google2.com");
		for (InetAddress inetAddress : addrs) {
			if (inetAddress.getHostAddress().equals("10.0.0.2")) {
				success = true;
				break;
			}
		}
		assertTrue(success);
		
		NameStore.getInstance().put("mydomain.com", "10.10.10.10");
		performLookupLocal("mydomain.com");
		addrs = localNS.lookupAllHostAddr("mydomain.com");
		for (InetAddress inetAddress : addrs) {
			if (inetAddress.getHostAddress().equals("10.10.10.10")) {
				success = true;
				break;
			}
		}
		assertTrue(success);
		
		//naver.com is not registered by us, it will use system DNS
		performLookupLocal("naver.com");

		NameStore.getInstance().remove("www.google.com");
		NameStore.getInstance().remove("www.google2.com");
	}

	@Test
	public void testLocalDNSLookupIp() throws UnknownHostException {
		System.out.println("test LocalDNSLookup with IP.....");
		cleanCustomDNS();

		NameStore.getInstance().put("www.google2.com", "10.0.0.2");
		
		String domain = localNS.getHostByAddr(DnsUtil.textToNumericFormat("10.0.0.2"));
		assertThat(domain, is("www.google2.com"));
		
		//naver.com is not registered by us, it will use system DNS
		InetAddress[] addrs = localNS.lookupAllHostAddr("naver.com");
		String ip1 = addrs[0].getHostAddress();
		try {
			domain = localNS.getHostByAddr(DnsUtil.textToNumericFormat(ip1));
		} catch (UnknownHostException e) {
			//FIXME Is it right? can not resolve in unit test.
		}
		//assertThat(domain, is("naver.com"));

		NameStore.getInstance().remove("www.google2.com");
	}

	public static void performLookup(String hostName) throws UnknownHostException {
		InetAddress[] addrs = InetAddress.getAllByName(hostName);
		for (int j = 0; j < addrs.length; j++) {
			System.out.println(addrs[j].toString());
		}
	}

	public void performLookupLocal(String hostName) throws UnknownHostException {
		InetAddress[] addrs = localNS.lookupAllHostAddr(hostName);
		System.out.println(hostName + ":");
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
