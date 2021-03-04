package org.ngrinder.http;

import java.util.concurrent.atomic.AtomicLong;

public class TimeToFirstByteHolder {
	public static final AtomicLong timeToFirstByte = new AtomicLong(0);

	public static void accumulate(long time) {
		timeToFirstByte.addAndGet(time);
	}

	public static long getTotal() {
		return timeToFirstByte.getAndSet(0);
	}
}
