package net.grinder.util;

import static net.grinder.util.NoOp.noOp;

/**
 * Thread Utility class.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public abstract class ThreadUtils {
	/**
	 * Sleep for the given millisecond.
	 * 
	 * @param millisecond
	 *            millisecond to sleep
	 */
	public static void sleep(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (InterruptedException e) {
			noOp();
		}
	}
}
