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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * This class is used to store the IP/port of target monitor JMX server, and the recorder.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class MonitorAgentInfo {
	private String ip;
	private int port;
	private MonitorCollection collection;
	private MonitorRecorder recoder;

	private MonitorAgentInfo(String ip, int port, MonitorCollection collection, MonitorRecorder recoder) {
		this.ip = ip;
		this.port = port;
		this.collection = collection;
		this.recoder = recoder;
	}

	/**
	 * create and return the {@link MonitorAgentInfo}.
	 * @param ip is the IP address of monitor target
	 * @param port is the port number of JMX monitor listener on target
	 * @param recoder is the recorder for the collected data
	 * @return monitorAgentInfo is {@link MonitorAgentInfo}
	 */
	public static MonitorAgentInfo getSystemMonitor(String ip, int port, MonitorRecorder recoder) {
		return new MonitorAgentInfo(ip, port, MonitorCollection.getSystemMonitorCollection(), recoder);
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public MonitorCollection getCollection() {
		return collection;
	}

	public MonitorRecorder getRecoder() {
		return recoder;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
