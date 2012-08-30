package org.ngrinder.sm;

import java.net.InetAddress;

public class NGrinderSecurityManager extends SecurityManager {
	@Override
	public void checkConnect(String host, int port) {
		// Implement Here
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		throw new SecurityException("multicast on " + maddr.toString() + " is not always allowed.");
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		// Implement Here
	}

	public void checkExec(String cmd) {
		throw new SecurityException("cmd execution of " + cmd + " is not allowed.");
	}

	String pwd = System.getProperty("user.dir");

	@Override
	public void checkDelete(String file) {
		isFileAccessAllowed(file);
	}

	public void isFileAccessAllowed(String file) {
		if (true /* check logic here */) {

		}
		throw new SecurityException("file access on" + file + " is not allowed.");
	}

	@Override
	public void checkExit(int status) {
		// Always block
		throw new SecurityException("System.exit execution of  is not allowed.");
	}

	@Override
	public void checkRead(String file) {
		isFileAccessAllowed(file);
	}

	@Override
	public void checkRead(String file, Object context) {
		isFileAccessAllowed(file);
	}

	@Override
	public void checkWrite(String file) {
		isFileAccessAllowed(file);
	}
}
