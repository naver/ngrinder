package com.nhncorp.ngrinder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(ThreadUtil.class);
	
	public static void sleep (long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOG.warn(e.getMessage(), e);
		}
	}

}
