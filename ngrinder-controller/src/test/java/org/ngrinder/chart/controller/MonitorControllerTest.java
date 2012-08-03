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
package org.ngrinder.chart.controller;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

/**
 * MonitorController Test class.
 *
 * @author Mavlarn
 * @since
 */
public class MonitorControllerTest {
	
	@Autowired
	private MonitorController monitorController;

	@Test
	public void testGetChartData() {
		ModelMap model = new ModelMap();
		monitorController.getChartData(model, null);
	}

	@Test
	public void testGetMonitorData() {
		ModelMap model = new ModelMap();
		Date endDate = new Date();
		Date stDate = new Date(endDate.getTime() - 60 * 1000 * 1000);
		monitorController.getMonitorData(model, "127.0.0.1", stDate, endDate, 0);
	}

	@Test
	public void testGetMonitorDataJava() {
		ModelMap model = new ModelMap();
		Date endDate = new Date();
		Date stDate = new Date(endDate.getTime() - 60 * 1000 * 1000);
		monitorController.getMonitorDataJava(model, "127.0.0.1", stDate, endDate, 0);
	}

	@Test
	public void testGetMonitorDataSystem() {
		ModelMap model = new ModelMap();
		Date endDate = new Date();
		Date stDate = new Date(endDate.getTime() - 60 * 1000 * 1000);
		monitorController.getMonitorDataSystem(model, "127.0.0.1", stDate, endDate, 0);
	}

}
