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
package org.ngrinder.extension;


import org.ngrinder.model.PerfTest;
import org.ngrinder.service.IPerfTestService;

/**
 * Plugin extension for {@link PerfTest} start and finish.
 * 
 * This plugin is necessary if you want to notify the test start and end.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnTestLifeCycleRunnable {

	/**
	 * Callback method which will be invoked whenever {@link PerfTest} is started.
	 * 
	 * @param perfTest
	 *            Performance Test
	 * @param perfTestService
	 *            perfTestService interface
	 * @param version
	 *            ngrinder version
	 */
	public void start(PerfTest perfTest, IPerfTestService perfTestService, String version);

	/**
	 * Callback method which will be invoked whenever {@link PerfTest} is finished.
	 * 
	 * 
	 * @param perfTest
	 *            Performance Test
	 * @param stopReason
	 *            stop reason
	 * @param perfTestService
	 *            perfTestService interface
	 * @param vesion
	 *            ngrinder version 
	 */
	public void finish(PerfTest perfTest, String stopReason, IPerfTestService perfTestService, String vesion);
}
