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

public class MonitorAgentInfo {
	private String ip;
	private int port;
	private MonitorCollection collection;
	private MonitorRecoder recoder;

	// public MonitorAgentInfo(String ip, int port, MonitorRecoder recoder) {
	// this(ip, port, MonitorCollection.getAgentMonitorCollection(), recoder);
	// }

	private MonitorAgentInfo(String ip, int port, MonitorCollection collection, MonitorRecoder recoder) {
		this.ip = ip;
		this.port = port;
		this.collection = collection;
		this.recoder = recoder;
	}

	public static MonitorAgentInfo getAgentMonitor(String ip, int port, MonitorRecoder recoder) {
		return new MonitorAgentInfo(ip, port, MonitorCollection.getAgentMonitorCollection(), recoder);
	}

	public static MonitorAgentInfo getTargetMonitor(String ip, int port, MonitorRecoder recoder) {
		return new MonitorAgentInfo(ip, port, MonitorCollection.getTargetMonitorCollection(), recoder);
	}

	public static MonitorAgentInfo getJavaMonitor(String ip, int port, MonitorRecoder recoder) {
		return new MonitorAgentInfo(ip, port, MonitorCollection.getJavaMonitorCollection(), recoder);
	}

	public static MonitorAgentInfo getSystemMonitor(String ip, int port, MonitorRecoder recoder) {
		return new MonitorAgentInfo(ip, port, MonitorCollection.getSystemMonitorCollection(), recoder);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public MonitorCollection getCollection() {
		return collection;
	}

	public void setCollection(MonitorCollection collection) {
		this.collection = collection;
	}

	public MonitorRecoder getRecoder() {
		return recoder;
	}

	public void setRecoder(MonitorRecoder recoder) {
		this.recoder = recoder;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
