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
package org.ngrinder.monitor.controller.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.ngrinder.model.BaseEntity;

@Entity
@Table(name = "JavaMonitor")
public class JavaDataModel extends BaseEntity<JavaDataModel> {

	private static final long serialVersionUID = -2872194513786272772L;

	private String key;
	private String ip;
	private int port;
	private String displayName;
	private long collectTime;
	private long heapMaxMemory;
	private long heapUsedMemory;
	private long nonHeapMaxMemory;
	private long nonHeapUsedMemory;
	private float cpuUsedPercentage;
	private int pid;
	private long threadCount;
	private long uptime;
	private String crtime;
	private String message;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public long getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}

	public long getHeapMaxMemory() {
		return heapMaxMemory;
	}

	public void setHeapMaxMemory(long heapMaxMemory) {
		this.heapMaxMemory = heapMaxMemory;
	}

	public long getHeapUsedMemory() {
		return heapUsedMemory;
	}

	public void setHeapUsedMemory(long heapUsedMemory) {
		this.heapUsedMemory = heapUsedMemory;
	}

	public long getNonHeapMaxMemory() {
		return nonHeapMaxMemory;
	}

	public void setNonHeapMaxMemory(long nonHeapMaxMemory) {
		this.nonHeapMaxMemory = nonHeapMaxMemory;
	}

	public long getNonHeapUsedMemory() {
		return nonHeapUsedMemory;
	}

	public void setNonHeapUsedMemory(long nonHeapUsedMemory) {
		this.nonHeapUsedMemory = nonHeapUsedMemory;
	}

	public float getCpuUsedPercentage() {
		return cpuUsedPercentage;
	}

	public void setCpuUsedPercentage(float cpuUsedPercentage) {
		this.cpuUsedPercentage = cpuUsedPercentage;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public long getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(long threadCount) {
		this.threadCount = threadCount;
	}

	public long getUptime() {
		return uptime;
	}

	public void setUptime(long uptime) {
		this.uptime = uptime;
	}

	public String getCrtime() {
		return crtime;
	}

	public void setCrtime(String crtime) {
		this.crtime = crtime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
