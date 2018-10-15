package org.ngrinder.starter;

import com.beust.jcommander.ParameterException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class PortAvailabilityValidator extends PortRangeValidator {
	@Override
	public void validate(String name, Integer value) throws ParameterException {
		if (value == null) {
			return;
		}
		super.validate(name, value);
		if (!checkExactPortAvailability(null, value)) {
			throw new ParameterException(name + "=" + value + " port is already occupied by the other system " +
					"or failed to bind. Please use the other port");
		}
	}

	/**
	 * Check if the given port is available.
	 *
	 * @param inetAddress address to be bound
	 * @param port port to be checked
	 * @return true if available
	 */
	public static boolean checkExactPortAvailability(InetAddress inetAddress, int port) {
		ServerSocket socket = null;
		try {
			if (inetAddress == null) {
				socket = new ServerSocket(port);
			} else {
				socket = new ServerSocket(port, 1, inetAddress);
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// FALL THROUGH
				}
			}
		}
	}

}
