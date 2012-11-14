package net.grinder.util;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class NetworkUtilTest {
	@Test
	public void testLocalHostAddress() {
		try {
			System.out.println("Local addr:" + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String localHostAddress = NetworkUtil.getLocalHostAddress();
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
		localHostAddress = NetworkUtil.getLocalHostAddress();
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
	}
	@Test
	public void testLocalHostName() {
		try {
			System.out.println("Local host:" + InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String localHostAddress = NetworkUtil.getLocalHostName();
		System.out.println("NetworkUtil.getLocalHostName:" + localHostAddress);
		assertThat(localHostAddress, notNullValue());
	}
}
