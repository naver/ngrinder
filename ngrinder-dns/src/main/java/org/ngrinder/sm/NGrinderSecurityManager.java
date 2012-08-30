package org.ngrinder.sm;

import java.net.InetAddress;

public class NGrinderSecurityManager extends SecurityManager {
	@Override
	public void checkConnect(String host, int port) {

	}

	@Override
	public void checkMulticast(InetAddress maddr) {

	}

	@Override
	public void checkConnect(String host, int port, Object context) {

	}

	public void checkExec(String cmd) {
		throw new SecurityException("cmd execution of " + cmd + " is not allowed.");
	}

	String pwd = System.getProperty("user.dir");

	@Override
	public void checkDelete(String file) {

		if (!file.startsWith(pwd)) {
			throw new SecurityException("deletion on" + file + " is not allowed.");
		}
	}

	@Override
	public void checkExit(int status) {
		// Always block
	}

	@Override
	public void checkRead(String file) {

	}

	@Override
	public void checkRead(String file, Object context) {

	}

	@Override
	public void checkWrite(String file) {

	}
}
