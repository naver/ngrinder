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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.ngrinder.model.BaseEntity;

@Entity
@Table(name = "system_monitor")
public class SystemDataModel extends BaseEntity<SystemDataModel> {

	private static final long serialVersionUID = 790334226137464982L;

	@Column(name = "monitor_key")
	@Index(name = "monitor_key_index")
	private String key;
	@Index(name = "ip_index")
	private String ip;
	private int port;
	private String system;
	@Index(name = "collect_time_index")
	private long collectTime;
	private float totalCpuValue;
	private float idleCpuValue;
	private double loadAvg1;
	private double loadAvg5;
	private double loadAvg15;
	private long freeMemory;
	private long totalMemory;
	private float cpuUsedPercentage;
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

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public long getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}

	public float getTotalCpuValue() {
		return totalCpuValue;
	}

	public void setTotalCpuValue(float totalCpuValue) {
		this.totalCpuValue = totalCpuValue;
	}

	public float getIdleCpuValue() {
		return idleCpuValue;
	}

	public void setIdleCpuValue(float idleCpuValue) {
		this.idleCpuValue = idleCpuValue;
	}

	public double getLoadAvg1() {
		return loadAvg1;
	}

	public void setLoadAvg1(double loadAvg1) {
		this.loadAvg1 = loadAvg1;
	}

	public double getLoadAvg5() {
		return loadAvg5;
	}

	public void setLoadAvg5(double loadAvg5) {
		this.loadAvg5 = loadAvg5;
	}

	public double getLoadAvg15() {
		return loadAvg15;
	}

	public void setLoadAvg15(double loadAvg15) {
		this.loadAvg15 = loadAvg15;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public float getCpuUsedPercentage() {
		return cpuUsedPercentage;
	}

	public void setCpuUsedPercentage(float cpuUsedPercentage) {
		this.cpuUsedPercentage = cpuUsedPercentage;
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
