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
package org.ngrinder.monitor.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.management.openmbean.CompositeData;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.ngrinder.monitor.controller.domain.MonitorCollection;
import org.ngrinder.monitor.controller.domain.MonitorCollectionInfoDomain;
import org.ngrinder.monitor.controller.domain.MonitorRecorder;
import org.ngrinder.monitor.share.CachedMBeanClient;
import org.ngrinder.monitor.share.domain.MBeanClient;
import org.ngrinder.monitor.share.domain.MonitorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This is monitor executing worker class.
 * During monitoring, there maybe more than one test want to monitor the same target server. But we just use one
 * monitor worker for one target server. And the member "referenceCounter" will save the number of tests which
 * are monitoring this target. 
 *
 * @author Mavlarn
 * @since 2.0
 */
public class MonitorExecuteWorker implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorExecuteWorker.class);

	private MBeanClient mbeanClient;
	private MonitorCollection collection;
	private MonitorRecorder recoder;
	private MonitorAgentInfo agentInfo;
	private String key;
	
	//used to save the counter of how many reference to use this monitor worker.
	private int referenceCounter = 1;

	/**
	 * Construct a MonitorExecuteWorker with the key and agent info.
	 * @param key is a key to mark the monitoring worker
	 * @param agentInfo	is the monitoring target
	 */
	public MonitorExecuteWorker(final String key, final MonitorAgentInfo agentInfo) {
		this.key = key;
		this.agentInfo = agentInfo;
		collection = agentInfo.getCollection();
		recoder = agentInfo.getRecoder();

		recoder.before();

		try {
			mbeanClient = CachedMBeanClient.getMBeanClient(agentInfo.getIp(), agentInfo.getPort());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * add a monitor reference on this monitor worker.
	 */
	public void increaseCounter() {
		referenceCounter++;
	}
	
	/**
	 * decrease the reference counter. it is used when a monitoring job will not monitor a target.
	 * When all the monitoring on that target is finished, the reference counter will be 0, and the
	 * monitoring worker will be stopped. 
	 */
	public void decreaseCounter() {
		referenceCounter--;
	}
	
	public int getCounter() {
		return referenceCounter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		// First try to connect
		if (!mbeanClient.isConnected()) {
			mbeanClient.connect();
		}

		// If it can not be connected, make an error message.
		if (!mbeanClient.isConnected()) {
			LOG.error("mbeanClient is not connected.");
			return;
		}

		// mbeanClient.flush();
		List<MonitorCollectionInfoDomain> mxBeans = collection.getMXBean();
		for (MonitorCollectionInfoDomain mxBean : mxBeans) {
			try {
				CompositeData cd = (CompositeData) mbeanClient.getAttribute(mxBean.getObjectName(),
						mxBean.getAttrName());

				Class<? extends MonitorInfo> returnClass = (Class<? extends MonitorInfo>) Class.forName(cd
						.getCompositeType().getTypeName());
				MonitorInfo retData = returnClass.newInstance();
				retData.parse(cd);

				retData.setCollectTime(System.currentTimeMillis());

				StringBuffer methodName = new StringBuffer().append(MonitorConstants.RECODER_METHOD_PREFIX).append(
						retData.getClass().getSimpleName());

				Method method = recoder.getClass().getMethod(methodName.toString(), key.getClass(),
						retData.getClass(),	agentInfo.getClass());

				if (method != null) {
					method.invoke(recoder, key, retData, agentInfo);
				}
			} catch (Exception e) {
				LOG.error("Error while MonitorExecutorWorker is runnng. Disconnect this MBean client.", e);
				mbeanClient.disconnect();
				break;
			}
		}
	}

	/**
	 * finish the monitoring worker, save unsaved monitor data and close the JMX or local JVM connection.
	 */
	public void close() {
		recoder.after();
		mbeanClient.disconnect();
	}
}
