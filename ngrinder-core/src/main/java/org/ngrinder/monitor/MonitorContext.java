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

public class MonitorContext {

	private MonitorContext() {
	}

	private static final MonitorContext INSTANCE = new MonitorContext();

	public static MonitorContext getInstance() {
		return INSTANCE;
	}

	private Set<String> dataCollectors = new HashSet<String>();

	public void setDataCollectors(Set<String> dataCollectors) {
		this.dataCollectors = dataCollectors;
	}

	public void setJvmPids(Set<Integer> jvmPids) {
		this.jvmPids = jvmPids;
	}

	private Set<Integer> jvmPids = new HashSet<Integer>();

	public Set<String> getDataCollectors() {
		return dataCollectors;
	}

	public void addDataCollector(String dataCollector) {
		this.dataCollectors.add(dataCollector);
	}

	public void removeDataCollector(String dataCollector) {
		this.dataCollectors.remove(dataCollector);
	}

	public Set<Integer> getJvmPids() {
		return jvmPids;
	}

	public void addJvmPid(Integer jvmPid) {
		this.jvmPids.add(jvmPid);
	}

	public void removeJvmPid(Integer jvmPid) {
		this.jvmPids.remove(jvmPid);
	}

}
