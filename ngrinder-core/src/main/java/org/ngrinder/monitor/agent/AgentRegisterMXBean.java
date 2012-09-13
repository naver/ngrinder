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
package org.ngrinder.monitor.agent;

import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.MonitorContext;
import org.ngrinder.monitor.agent.mxbean.SystemMonitoringData;
import org.ngrinder.monitor.agent.mxbean.core.MXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AgentRegisterMXBean {
	private static final Logger LOG = LoggerFactory.getLogger(AgentRegisterMXBean.class);
	private static final AgentRegisterMXBean INSTANCE = new AgentRegisterMXBean();

	private AgentRegisterMXBean() {
	}

	public void addDefaultMXBean(MBeanServer mbeanServer) throws MalformedObjectNameException,
			InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
			NullPointerException {
		Set<String> dataCollectors = MonitorContext.getInstance().getDataCollectors();

		if (dataCollectors.contains(MonitorConstants.SYSTEM)) {
			addMXBean(mbeanServer, MonitorConstants.SYSTEM, new SystemMonitoringData());
		}

		LOG.info("Default MXBean Initialized.");
	}

	void addMXBean(MBeanServer mbeanServer, String subDomainName, MXBean bean) throws MalformedObjectNameException,
			NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		ObjectName objectName = new ObjectName(MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + subDomainName);
		mbeanServer.registerMBean(bean, objectName);

		AgentMXBeanStorage.getInstance().addMXBean(subDomainName, bean);

		LOG.info("Added MXBean:{}.", objectName);
	}

	public static AgentRegisterMXBean getInstance() {
		return INSTANCE;
	}
}
