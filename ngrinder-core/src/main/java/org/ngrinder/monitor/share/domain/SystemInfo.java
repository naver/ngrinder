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
package org.ngrinder.monitor.share.domain;

import java.io.Serializable;

import javax.management.openmbean.CompositeData;

import org.apache.commons.lang.builder.ToStringBuilder;

public class SystemInfo extends MonitorInfo implements Serializable {

	private static final long serialVersionUID = -2995334644975166549L;

	public enum System {
		LINUX, WINDOW
	}

	private System system;

	private long totalCpuValue;

	private long idleCpuValue;

	private double[] loadAvgs = new double[3];

	private long freeMemory;

	private long totalMemory;

	private float cpuUsedPercentage;

	public void parse(CompositeData cd) {
		if (cd == null) {
			return;
		}
		if (cd.containsKey("collectTime")) {
			this.collectTime = getLong(cd, "collectTime");
		}
		if (cd.containsKey("system")) {
			this.system = System.valueOf(getString(cd, "system"));
		}
		if (cd.containsKey("totalCpuValue")) {
			this.totalCpuValue = getLong(cd, "totalCpuValue");
		}
		if (cd.containsKey("idlecpu")) {
			this.idleCpuValue = getLong(cd, "idlecpu");
		}
		if (cd.containsKey("freeMemory")) {
			this.freeMemory = getLong(cd, "freeMemory");
		}
		if (cd.containsKey("totalMemory")) {
			this.totalMemory = getLong(cd, "totalMemory");
		}
		if (cd.containsKey("CPUUsedPercentage")) {
			this.cpuUsedPercentage = getFloat(cd, "CPUUsedPercentage");
		}
		if (cd.containsKey("loadAvgs")) {
			this.setLoadAvgs((double[]) cd.get("loadAvgs"));
		}
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		this.system = system;
	}

	public void setSystem(String system) {
		this.system = System.valueOf(system);
	}

	public void setCPUUsedPercentage(float cpuUsedPercentage) {
		this.cpuUsedPercentage = cpuUsedPercentage;
	}

	public float getCPUUsedPercentage() {
		return cpuUsedPercentage;
	}

	public long getIdlecpu() {
		return idleCpuValue;
	}

	public long getTotalCpuValue() {
		return totalCpuValue;
	}

	public void setLoadAvgs(double[] loadAvgs) {
		this.loadAvgs = loadAvgs;
	}
	
	public double[] getLoadAvgs() {
		return loadAvgs;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setTotalCpuValue(long totalCpuValue) {
		this.totalCpuValue = totalCpuValue;
	}

	public void setIdleCpuValue(long idleCpuValue) {
		this.idleCpuValue = idleCpuValue;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


}
