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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.openmbean.CompositeData;

public class JavaInfo extends MonitorInfo implements Serializable {
	private static final long serialVersionUID = -8984112358527689876L;

	private List<JavaInfoForEach> javaInfoForEach;

	public List<JavaInfoForEach> getJavaInfoForEach() {
		return javaInfoForEach;
	}

	public void setJavaInfoForEach(List<JavaInfoForEach> javaInfoForEach) {
		this.javaInfoForEach = javaInfoForEach;
	}

	public void addJavaInfoForEach(JavaInfoForEach javaInfoForEach) {
		if (this.javaInfoForEach == null) {
			this.javaInfoForEach = Collections.synchronizedList(new ArrayList<JavaInfoForEach>());
		}
		this.javaInfoForEach.add(javaInfoForEach);
	}

	public void parse(CompositeData cd) {
		if (cd == null) {
			return;
		}
		this.collectTime = getLong(cd, "collectTime");
		if (cd.containsKey("javaInfoForEach")) {
			List<JavaInfoForEach> tmpJavaInfoForEach = Collections.synchronizedList(new ArrayList<JavaInfoForEach>());
			CompositeData[] cdJavaInfoEachs = (CompositeData[]) cd.get("javaInfoForEach");
			if (null == cdJavaInfoEachs) {
				return;
			}

			for (CompositeData cdJavaInfoEach : cdJavaInfoEachs) {
				JavaInfoForEach javaInfoForEach = new JavaInfoForEach();
				javaInfoForEach.setDisplayName(getString(cdJavaInfoEach, "displayName"));
				javaInfoForEach.setJavaCpuUsedPercentage(getFloat(cdJavaInfoEach, "javaCpuUsedPercentage"));
				javaInfoForEach.setPid(getInt(cdJavaInfoEach, "pid"));
				javaInfoForEach.setThreadCount(getInt(cdJavaInfoEach, "threadCount"));
				javaInfoForEach.setUptime(getLong(cdJavaInfoEach, "uptime"));
				CompositeData cdHeapMemory = (CompositeData) cdJavaInfoEach.get("heapMemory");

				JavaMemory heapMemory = new JavaMemory();
				if (cdHeapMemory != null) {
					heapMemory.setCommitted(getLong(cdHeapMemory, "committed"));
					heapMemory.setInit(getLong(cdHeapMemory, "init"));
					heapMemory.setMax(getLong(cdHeapMemory, "max"));
					heapMemory.setUsed(getLong(cdHeapMemory, "used"));
				}
				javaInfoForEach.setHeapMemory(heapMemory);

				CompositeData cdNonHeapMemory = (CompositeData) cdJavaInfoEach.get("nonHeapMemory");
				JavaMemory nonHeapMemory = new JavaMemory();
				if (cdNonHeapMemory != null) {
					nonHeapMemory.setCommitted(getLong(cdNonHeapMemory, "committed"));
					nonHeapMemory.setInit(getLong(cdNonHeapMemory, "init"));
					nonHeapMemory.setMax(getLong(cdNonHeapMemory, "max"));
					nonHeapMemory.setUsed(getLong(cdNonHeapMemory, "used"));
				}
				javaInfoForEach.setNonHeapMemory(nonHeapMemory);

				tmpJavaInfoForEach.add(javaInfoForEach);
			}
			this.javaInfoForEach = tmpJavaInfoForEach;
		}
	}
}
