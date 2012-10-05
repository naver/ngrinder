/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.monitor;

import java.util.HashSet;
import java.util.Set;

import org.ngrinder.infra.AgentConfig;

/**
 * Static Monitoring Constants.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class MonitorConstants {

	public static final int DEFAULT_MONITOR_PORT = 13243;
	public static final int DEFAULT_MONITOR_COLLECTOR_INTERVAL = 1;
	public static final String DEFAULT_MONITOR_DOMAIN = "org.ngrinder.monitor";
	public static final String DEFALUT_MONITOR_DISPLAY_NAME = "nGrinder monitoring agent";
	public static final String RECODER_METHOD_PREFIX = "recoder";
	public static final String SYSTEM = "name=System";
	public static final int DEFAULT_CONTROLLER_CACHE_SIZE = 128;
	public static final int DEFAULT_CONTROLLER_INTERVAL = 1;

	public static final Set<String> SYSTEM_DATA_COLLECTOR = new HashSet<String>();
	static {
		SYSTEM_DATA_COLLECTOR.add(SYSTEM);
	}

	//default collector will only collect system data.
	public static final Set<String> DEFAULT_DATA_COLLECTOR = SYSTEM_DATA_COLLECTOR;

	/**
	 * empty means all processes.
	 */
	public static final Set<Integer> DEFAULT_JVM_PID = new HashSet<Integer>();

	public static final String P_COMMA = ",";

	/**
	 * Initialize the Monitor configuration.
	 * 
	 * @param agentConfig
	 *            {@link AgentConfig} from which the property is loaded.
	 */
	public static void init(AgentConfig agentConfig) {
//		PropertiesWrapper agentProperties = agentConfig.getAgentProperties();
//		DEFAULT_MONITOR_PORT = agentProperties.getPropertyInt("monitor.listen.port", DEFAULT_MONITOR_PORT);
//		DEFAULT_MONITOR_COLLECTOR_INTERVAL = agentProperties.getPropertyInt("monitor.collector.interval", DEFAULT_MONITOR_COLLECTOR_INTERVAL);
//		DEFAULT_CONTROLLER_CACHE_SIZE = agentProperties.getPropertyInt("monitor.controller.cache.size", DEFAULT_CONTROLLER_CACHE_SIZE);
//		DEFAULT_CONTROLLER_INTERVAL = agentProperties.getPropertyInt("monitor.collector.interval", DEFAULT_CONTROLLER_INTERVAL);
	}
}
