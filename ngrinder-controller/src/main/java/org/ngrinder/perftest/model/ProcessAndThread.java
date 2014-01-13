/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.perftest.model;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Processes and Threads count model.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("UnusedDeclaration")
public class ProcessAndThread {
	/**
	 * Constructor.
	 * 
	 * @param processCount
	 *            count of processes
	 * @param threadCount
	 *            count of threads
	 */
	public ProcessAndThread(int processCount, int threadCount) {
		this.processCount = processCount;
		this.threadCount = threadCount;
	}

	/** Count of processes. */
	private int processCount;
	/** Count of threads. */
	private int threadCount;

	public int getProcessCount() {
		return processCount;
	}

	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
