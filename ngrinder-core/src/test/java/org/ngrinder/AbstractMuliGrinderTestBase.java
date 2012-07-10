package org.ngrinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.ngrinder.common.util.ThreadUtil;

abstract public class AbstractMuliGrinderTestBase {
	/**
	 * Sleep quietly
	 * 
	 * @param milisecond
	 */
	public void sleep(long milisecond) {
		ThreadUtil.sleep(milisecond);
	}

	/**
	 * Returns a free port numbers on localhost, or less than give count entries if unable to find a free port.
	 * 
	 * @return a free port number on localhost, or less than give count entries if unable to find a free port
	 */
	public List<Integer> findFreePorts(int count) {
		List<Integer> ports = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			ports.add(findFreePort());
		}
		return ports;
	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free port.
	 * 
	 * @return a free port number on localhost, or -1 if unable to find a free port
	 */
	public int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

}
