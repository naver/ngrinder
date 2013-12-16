package org.ngrinder.common.constants;

public interface MonitorConstants {
	public static final String PROP_MONITOR_BINDING_IP = "monitor.binding_ip";
	public static final String PROP_MONITOR_BINDING_PORT = "monitor.binding_port";

	/**
	 * Monitor Constant
	 */
	public static final int DEFAULT_MONITOR_COLLECTOR_INTERVAL = 1;
	public static final String DEFAULT_MONITOR_DOMAIN = "org.ngrinder.monitor";
	public static final String SYSTEM = "name=System";
	String MONITOR_FILE_PREFIX = "monitor_system_";
}
