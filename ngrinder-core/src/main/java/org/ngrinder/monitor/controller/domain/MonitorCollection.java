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
package org.ngrinder.monitor.controller.domain;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.share.domain.JavaInfo;
import org.ngrinder.monitor.share.domain.SystemInfo;

public class MonitorCollection {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorCollection.class);
	protected List<MonitorCollectionInfoDomain> collectionList = new ArrayList<MonitorCollectionInfoDomain>();

	private static final MonitorCollection AGENT_MONITOR_COLLECTION_INSTANCE = new MonitorCollection();
	private static final MonitorCollection TARGET_MONITOR_COLLECTION_INSTANCE = new MonitorCollection();
	private static final MonitorCollection JAVA_MONITOR_COLLECTION_INSTANCE = new MonitorCollection();
	private static final MonitorCollection SYSTEM_MONITOR_COLLECTION_INSTANCE = new MonitorCollection();

	static {
		AGENT_MONITOR_COLLECTION_INSTANCE.addJavaMonitorCollection();
		AGENT_MONITOR_COLLECTION_INSTANCE.addSystemMonitorCollection();

		TARGET_MONITOR_COLLECTION_INSTANCE.addSystemMonitorCollection();

		JAVA_MONITOR_COLLECTION_INSTANCE.addJavaMonitorCollection();

		SYSTEM_MONITOR_COLLECTION_INSTANCE.addSystemMonitorCollection();

	}

	private MonitorCollection() {
	}

	static MonitorCollection getAgentMonitorCollection() {
		return AGENT_MONITOR_COLLECTION_INSTANCE;
	}

	static MonitorCollection getTargetMonitorCollection() {
		return TARGET_MONITOR_COLLECTION_INSTANCE;
	}

	static MonitorCollection getJavaMonitorCollection() {
		return JAVA_MONITOR_COLLECTION_INSTANCE;
	}

	static MonitorCollection getSystemMonitorCollection() {
		return SYSTEM_MONITOR_COLLECTION_INSTANCE;
	}

	private void addJavaMonitorCollection() {
		try {
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.JAVA;
			ObjectName javaBasicName = new ObjectName(objNameStr);
			collectionList.add(new MonitorCollectionInfoDomain(javaBasicName, "JavaInfo", JavaInfo.class));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void addSystemMonitorCollection() {
		try {
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.SYSTEM;
			ObjectName systemName = new ObjectName(objNameStr);
			collectionList.add(new MonitorCollectionInfoDomain(systemName, "SystemInfo", SystemInfo.class));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public List<MonitorCollectionInfoDomain> getMXBean() {
		return collectionList;
	}
}
