package net.grinder.util;

import static net.grinder.util.NoOp.noOp;

public class ThreadUtils {
	public static void sleep(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (InterruptedException e) {
			noOp();
		}
	}
}
