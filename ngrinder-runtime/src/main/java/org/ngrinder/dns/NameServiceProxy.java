package org.ngrinder.dns;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.Class.forName;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Collections.singletonList;

/**
 * To use ngrinder local managed DNS as NameService implementation of {@link InetAddress}.
 *
 * @since 3.5.1
 */
public interface NameServiceProxy extends InvocationHandler {

	/**
	 * Lookup a host mapping by name. Retrieve the IP addresses
	 * associated with a host
	 *
	 * @param host the specified hostname
	 * @return array of IP addresses for the requested host
	 * @throws UnknownHostException
	 *             if no IP address for the {@code host} could be found
	 */
	InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException;

	/**
	 * Lookup the host corresponding to the IP address provided
	 *
	 * @param addr byte array representing an IP address
	 * @return {@code String} representing the host name mapping
	 * @throws UnknownHostException
	 *             if no host found for the specified IP address
	 */
	String getHostByAddr(byte[] addr) throws UnknownHostException;


	/**
	 * @param dns NameService implementation to be replaced.
	 * Replace NameService implementation of {@link InetAddress}.
	 */
	static void set(NameServiceProxy dns) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		Class<?> inetAddressClass = InetAddress.class;
		Object proxyInstance;
		Field nameServiceField;
		try {
			// for JDK 9 or above.
			Class<?> originNameServiceInterface = forName("java.net.InetAddress$NameService");
			nameServiceField = inetAddressClass.getDeclaredField("nameService");
			proxyInstance = newProxyInstance(originNameServiceInterface.getClassLoader(),
				new Class<?>[] { originNameServiceInterface }, dns);
		} catch (ClassNotFoundException | NoSuchFieldException e) {
			// for JDK 8 or less.
			Class<?> originNameServiceInterface = forName("sun.net.spi.nameservice.NameService");
			nameServiceField = inetAddressClass.getDeclaredField("nameServices");
			proxyInstance = singletonList(newProxyInstance(originNameServiceInterface.getClassLoader(),
				new Class<?>[] { originNameServiceInterface }, dns));
		}
		nameServiceField.setAccessible(true);
		nameServiceField.set(inetAddressClass, proxyInstance);
	}

	@Override
	default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.getName().equalsIgnoreCase("lookupAllHostAddr") ?
			lookupAllHostAddr((String) args[0]) : getHostByAddr((byte[]) args[0]);
	}
}
