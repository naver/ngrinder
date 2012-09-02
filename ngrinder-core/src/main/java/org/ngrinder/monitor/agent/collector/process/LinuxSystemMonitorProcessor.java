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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.mxbean.SystemMonitoringData;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinuxSystemMonitorProcessor implements Callable<SystemInfo> {
	private static final Logger LOG = LoggerFactory.getLogger(LinuxSystemMonitorProcessor.class);

	@Override
	public SystemInfo call() {
		StringBuilder result = new StringBuilder();
		BufferedReader input = null;
		try {
			File exeFile = new File("./system.sh");
			if (!exeFile.exists()) {
				InputStream inputStream = null;
				OutputStream outStream = null;
				try {
					inputStream = SystemMonitoringData.class.getResourceAsStream("/shell/system.sh");
					outStream = new FileOutputStream(exeFile);
					byte[] buf = new byte[1024];
					int len;
					while ((len = inputStream.read(buf)) > 0) {
						outStream.write(buf, 0, len);
					}
				} finally {
					IOUtils.closeQuietly(inputStream);
					IOUtils.closeQuietly(outStream);
				}
				exeFile.setExecutable(true);
			}
			Process proc = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", exeFile.getCanonicalPath() });
			String line;
			input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = input.readLine()) != null) {
				result.append(line).append("\n");
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(input);
		}
		return parse(result.toString());
	}

	private SystemInfo parse(String result) {
		SystemInfo systemInfo = new SystemInfo();
		String[] splittedLines = result.split("\n");
		for (int row = 0; row < splittedLines.length; row++) { // line number
			String[] splitteds = splittedLines[row].split(MonitorConstants.P_COMMA);
			if (splitteds.length < 1) {
				continue;
			}

			for (int column = 0; column < splitteds.length; column++) {
				if (splitteds[column].contains("kB")) {
					splitteds[column] = splitteds[column].replace("kB", "");
				}
				Object value;
				if (splitteds[column].contains(".")) {
					value = parseRealNumber(splitteds[column], Double.class);
				} else {
					value = parseRealNumber(splitteds[column], Long.class);
				}

				setData(row, column, value, systemInfo);
			}
		}

		float usedCpuPct = 0f;
		if (systemInfo.getTotalCpuValue() != 0) {
			long usedCpuValue = systemInfo.getTotalCpuValue() - systemInfo.getIdlecpu();
			usedCpuPct = (float) (usedCpuValue / (float) systemInfo.getTotalCpuValue()) * 100;
		}
		systemInfo.setCPUUsedPercentage(usedCpuPct);
		systemInfo.setSystem(SystemInfo.System.LINUX);
		return systemInfo;
	}

	private void setData(int row, int column, Object value, SystemInfo systemInfo) {
		long lvalue = 0L;
		double fvalue = 0.0;
		if (value instanceof Long) {
			lvalue = (Long) value;
		} else if (value instanceof Double) {
			fvalue = (Double) value;
		}

		if (row == 0) {// cpu total, cpu idle info
			if (column == 0) {
				systemInfo.setTotalCpuValue(lvalue);
			} else if (column == 1) {
				systemInfo.setIdleCpuValue(lvalue);
			}
		} else if (row == 1) { // load averages
			if (column == 0) {
				systemInfo.setLoadAvgs(fvalue);
			} else if (column == 1) {
				systemInfo.setLoadAvgs5(fvalue);
			} else if (column == 2) {
				systemInfo.setLoadAvgs15(fvalue);
			}
		} else if (row == 2) { // total free memory
			if (column == 0) {
				systemInfo.setTotalMemory(lvalue);
			} else if (column == 1) {
				systemInfo.setFreeMemory(lvalue);
			}
		}
	}

	private Object parseRealNumber(String string, Class<? extends Number> classType) {
		Object value = null;
		if (classType.isAssignableFrom(Long.class)) {
			value = Long.parseLong(string.trim());
		} else if (classType.isAssignableFrom(Double.class)) {
			value = Double.parseDouble(string.trim());
		}
		return value;
	}
}
