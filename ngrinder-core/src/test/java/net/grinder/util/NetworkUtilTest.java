package net.grinder.util;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class NetworkUtilTest {
	@Test
	public void testLocalHostName() {
		try {
			System.out.println("Local add:" + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String localHostAddress = NetworkUtil.getLocalHostAddress();
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
		localHostAddress = NetworkUtil.getLocalHostAddress();
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
	}
}
