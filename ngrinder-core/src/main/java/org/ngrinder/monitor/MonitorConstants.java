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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorConstants {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorConstants.class);

	public static int DEFAULT_AGENT_PORT;
	public static int DEFAULT_AGENT_COLLECTOR_INTERVAL;
	public static final String DEFAULT_MONITOR_DOMAIN = "org.ngrinder.monitor";
	public static final String DEFALUT_MONITOR_DISPLAY_NAME = "nGrinder monitoring agent";

	public static final String RECODER_METHOD_PREFIX = "recoder";

	public static final String JAVA = "name=Java";
	public static final String SYSTEM = "name=System";

	public static int DEFAULT_CONTROLLER_CACHE_SIZE;
	public static int DEFAULT_CONTROLLER_INTERVAL;

	public static final Set<String> TARGET_SERVER_DATA_COLLECTOR = new HashSet<String>();
	static {
		TARGET_SERVER_DATA_COLLECTOR.add(SYSTEM);
	}

	public static final Set<String> AGENT_SERVER_DATA_COLLECTOR = new HashSet<String>();
	static {
		AGENT_SERVER_DATA_COLLECTOR.add(SYSTEM);
		AGENT_SERVER_DATA_COLLECTOR.add(JAVA);
	}

	public static final Set<String> DEFAULT_DATA_COLLECTOR = AGENT_SERVER_DATA_COLLECTOR;

	/**
	 * empty means all processes
	 */
	public static Set<Integer> DEFAULT_JVM_PID = new HashSet<Integer>();

	public static String P_COMMA = ",";

	static {
		Properties prop = new Properties();
		InputStream myInputStream = null;
		try {
			myInputStream = MonitorConstants.class.getResourceAsStream("/monitor.properties");
			prop.load(myInputStream);

			DEFAULT_AGENT_PORT = Integer.parseInt(prop.getProperty("agent.listen.port", "3243"));
			DEFAULT_AGENT_COLLECTOR_INTERVAL = Integer.parseInt(prop.getProperty("agent.collector.interval", "1"));
			DEFAULT_CONTROLLER_CACHE_SIZE = Integer.parseInt(prop.getProperty("controller.cache.size", "128"));
			DEFAULT_CONTROLLER_INTERVAL = Integer.parseInt(prop.getProperty("controller.collector.interval", "1"));
		} catch (IOException e) {
			LOG.error("IOException during loading monitor.properties:" + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(myInputStream);
		}
	}
}
