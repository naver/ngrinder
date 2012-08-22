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
package org.ngrinder.monitor.agent.collector.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowSystemMonitorProcessor implements Callable<SystemInfo> {
	private static final Logger LOG = LoggerFactory.getLogger(WindowSystemMonitorProcessor.class);
	// typeperf "\Processor(_Total)\% Processor Time" "\Memory\Available KBytes"
	// -sc 1
	private static final String WINDOWS_TYPEPERF_PARAM = "\"\\Processor(_Total)\\% Processor Time\" "
			+ "\"\\Memory\\Available KBytes\"";

	private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

	@Override
	public SystemInfo call() {
		SystemInfo systemInfo = new SystemInfo();
		StringBuilder result = new StringBuilder();
		BufferedReader input = null;
		try {
			// String pcName = InetAddress.getLocalHost().getHostName();
			Process proc = Runtime.getRuntime().exec(
					new String[] { "cmd.exe", "/c", "typeperf " + WINDOWS_TYPEPERF_PARAM + "  -sc 1" });
			String line = null;
			
			input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = input.readLine()) != null) {
				result.append(line).append("\n");
			}

		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (null != input) {
					input.close();
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		parse(systemInfo, result.toString());
		systemInfo.setSystem(SystemInfo.System.WINDOW);
		return systemInfo;
	}

	private void parse(SystemInfo systemInfo, String str) {
		//
		// "(PDH-CSV 4.0)","\\kr14993-PC\processor(_Total)\% Processor Time"
		// "10/06/2010 14:42:47.201","0.000000","1172.000000"
		// 다려 주십시오.
		// 명령이 성공적으로 완료되었습니다.

		String toDay = formatter.format(new Date());

		String[] splittedLines = str.split("\n");
		for (int i = 0; i < splittedLines.length; i++) { // line number
			if (splittedLines[i].startsWith("\"" + toDay)) { // line find
				String[] splitteds = splittedLines[i].split(MonitorConstants.P_COMMA);
				if (splitteds.length > 1) {
					// CPU
					String cpu = splitteds[1].subSequence(1, splitteds[1].length() - 1).toString();
					systemInfo.setCPUUsedPercentage(Float.parseFloat(cpu));

					if (splitteds.length > 2) {
						// Free Memory (
						String strFreeMemoryKByte = splitteds[2].subSequence(1, splitteds[2].indexOf(".")).toString();
						systemInfo.setFreeMemory(Long.parseLong(strFreeMemoryKByte));
					}
				}
			}
		}
	}
}
