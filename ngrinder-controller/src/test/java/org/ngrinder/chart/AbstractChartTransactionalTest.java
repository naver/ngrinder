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
package org.ngrinder.chart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.math.RandomUtils;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public abstract class AbstractChartTransactionalTest extends AbstractNGrinderTransactionalTest {

	protected static final String DATE_FORMAT = "yyyyMMddHHmmss";
	protected static final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
	
	protected JavaDataModel newJavaData(long colTime, String ip) {
		JavaDataModel javaInfo = new JavaDataModel();
		javaInfo.setIp(ip);
		javaInfo.setCollectTime(colTime);
		javaInfo.setCpuUsedPercentage(RandomUtils.nextFloat());
		javaInfo.setHeapMaxMemory(2048000);
		int used = RandomUtils.nextInt(2048000);
		javaInfo.setHeapUsedMemory(used);
		javaInfo.setNonHeapMaxMemory(1024000);
		used = RandomUtils.nextInt(1024000);
		javaInfo.setNonHeapUsedMemory(1024000 - used);
		javaInfo.setThreadCount(RandomUtils.nextInt(20));
		javaInfo.setPort(12345);
		return javaInfo;
	}

	protected SystemDataModel newSysData(long colTime, String ip) {
		SystemDataModel sysInfo = new SystemDataModel();
		sysInfo.setIp(ip);
		sysInfo.setCollectTime(colTime);
		sysInfo.setCpuUsedPercentage(RandomUtils.nextFloat());
		sysInfo.setIdleCpuValue(RandomUtils.nextFloat());
		sysInfo.setPort(12345);
		sysInfo.setTotalMemory(4096000);
		sysInfo.setFreeMemory(4096000 - RandomUtils.nextInt(2048000));
		sysInfo.setTotalCpuValue(4);
		return sysInfo;
	}
	
}
