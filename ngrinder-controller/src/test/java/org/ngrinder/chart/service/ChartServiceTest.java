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
package org.ngrinder.chart.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.chart.repository.JavaMonitorRepository;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class ChartServiceTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private JavaMonitorRepository javaRepository;
	
	@Test
	@Rollback(false)
	public void testSaveJavaMonitorInfo() {
		JavaDataModel javaInfo = new JavaDataModel();
		javaInfo.setIp("10.0.0.1");
		javaInfo.setCollectTime(20120719010100L);
		javaInfo.setCpuUsedPercentage(0.1F);
		javaInfo.setHeapMaxMemory(2048);
		javaInfo.setHeapUsedMemory(1024);
		javaInfo.setNonHeapMaxMemory(1024);
		javaInfo.setNonHeapUsedMemory(526);
		javaInfo.setThreadCount(24);
		javaInfo.setPort(12345);
		javaRepository.save(javaInfo);
		
		JavaDataModel infoInDb = javaRepository.findOne(javaInfo.getId());
		assertTrue(infoInDb.getId().equals(javaInfo.getId()) &&
				infoInDb.getCpuUsedPercentage() == javaInfo.getCpuUsedPercentage());

	}

	/**
	 * Used to add mock monitor data
	 */
	//@Test
	public void addMockMonitorData() {
		int i = 1000 * 100;
		long colTime = 20120719010101L;
		while (i > 0) {
			JavaDataModel javaInfo = new JavaDataModel();
			javaInfo.setIp("10.0.0.1");
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
			javaRepository.save(javaInfo);
			colTime++;
		}
	}
	@Test
	public void testSaveSystemMonitorInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetJavaMonitorData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSystemMonitorData() {
		fail("Not yet implemented");
	}

}
