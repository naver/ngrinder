package org.ngrinder;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class PortRangeValidator implements IValueValidator<Integer> {
	@Override
	public void validate(String name, Integer value) throws ParameterException {
		if (value > Character.MAX_VALUE && value < 0) {
			throw new ParameterException(name + "=" + value + " port is used. The port should be within 0 and " +
					Character.MAX_VALUE);
		}
	}

}