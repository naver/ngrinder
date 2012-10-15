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
package org.ngrinder.monitor.controller;

import java.util.ArrayList;
import java.util.List;

import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.ngrinder.monitor.controller.domain.MonitorRecorder;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class description.
 * 
 * @author Tobi
 * @since
 * @date 2012-7-20
 */
public class MonitorRecoderDemo implements MonitorRecorder {
	
	private static final Logger LOG = LoggerFactory.getLogger(MonitorRecoderDemo.class);

	private boolean running = false;
	private List<String> data = new ArrayList<String>();

	@Override
	public void before() {
		running = true;
	}

	@Override
	public void recoderSystemInfo(String key, SystemInfo systemInfo, MonitorAgentInfo agentInfo) {
		if (running) {
			String record = String.format("Record system info: %s for key:%s", systemInfo, key);
			LOG.debug(record);
			data.add(record);
		}
	}

	@Override
	public void after() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public List<String> getData() {
		return data;
	}
}
