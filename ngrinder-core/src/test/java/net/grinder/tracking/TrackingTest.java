package net.grinder.tracking;

import org.junit.Test;
import org.ngrinder.tracking.FocusPoint;
import org.ngrinder.tracking.JGoogleAnalyticsTracker;

public class TrackingTest {
	@Test
	public void track() {
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("ngrinder", "3.0", "UA-36328271-1");
		for (int i = 1; i < 100; i++) {
			FocusPoint point = new FocusPoint("test");
			tracker.trackAsynchronously(point);
		}
	}
}
