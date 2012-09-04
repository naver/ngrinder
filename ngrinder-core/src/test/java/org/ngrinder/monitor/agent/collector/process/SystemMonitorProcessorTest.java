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

import java.util.concurrent.Callable;

import org.junit.Test;
import org.ngrinder.monitor.share.domain.SystemInfo;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class SystemMonitorProcessorTest {

	@Test
	public void test() {
		
		Callable<SystemInfo> processor = null;
		
		final String osSystem = System.getProperty("os.name");
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		if (osSystem.toLowerCase().indexOf("windows") > -1) {
			// windows
			processor = new WindowSystemMonitorProcessor();
		} else {
			// linux
			processor = new LinuxSystemMonitorProcessor();
		}
		
		try {
			SystemInfo info = processor.call();
			System.out.println("System info:" + info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * In hudson, there is only one kind of processor can be tested.
	 * In this test, we can test with other OS.
	 * Just use the test to let it run, check the process, but it can not execute and get anything.
	 */
	@Test
	public void testWithOtherOS() {
		Callable<SystemInfo> processor = null;
		
		final String osSystem = System.getProperty("os.name");
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		if (osSystem.toLowerCase().indexOf("windows") > -1) {
			// windows
			processor = new LinuxSystemMonitorProcessor();
		} else {
			// linux
			processor = new WindowSystemMonitorProcessor();
		}
		
		try {
			processor.call();
		} catch (Exception e) {
			//just skip, can not execute command one other OS.
		}
	}

}
