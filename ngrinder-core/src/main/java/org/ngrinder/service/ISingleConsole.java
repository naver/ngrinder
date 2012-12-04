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
package org.ngrinder.service;

import java.io.File;

import net.grinder.statistics.StatisticsIndexMap;

/**
 * {@link SingleConsole} interface for plugin use.
 * 
 */
public interface ISingleConsole {

	/**
	 * Mark the cancel status.
	 */
	public abstract void cancel();

	/**
	 * Get current running time in ms.
	 * 
	 * @return running time
	 */
	public abstract long getCurrentRunningTime();

	/**
	 * Get the current total execution count(test count + error count).
	 * 
	 * @return current total execution count
	 */
	public abstract long getCurrentExecutionCount();

	/**
	 * Get statistics index map used.
	 * 
	 * @return {@link StatisticsIndexMap}
	 */
	public StatisticsIndexMap getStatisticsIndexMap();

	/**
	 * Get report path.
	 * 
	 * @return report path
	 */
	public abstract File getReportPath();

	/**
	 * Get peak TPS.
	 * 
	 * @return peak tps
	 */
	public abstract double getPeakTpsForGraph();

	/**
	 * Get the count of current running threads.
	 * 
	 * @return running threads.
	 */
	public abstract int getRunningThread();

	/**
	 * Get the count of current running processes.
	 * 
	 * @return running processes
	 */
	public abstract int getRunningProcess();

}
