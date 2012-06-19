package com.nhncorp.ngrinder.test.model;

public class MonitorParams extends TestParams {

	private static final long serialVersionUID = -3645105931828849696L;

	private boolean monitor = false;

	/**
	 * 0: run only once <br>
	 * !0: always run
	 */
	private long runInterval;

}
