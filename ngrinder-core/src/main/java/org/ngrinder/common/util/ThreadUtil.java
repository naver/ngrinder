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
package org.ngrinder.common.util;

import static org.ngrinder.common.util.NoOp.noOp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread related utility.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class ThreadUtil {

	private static final int RETRY_MILLISECOND = 5000;
	private static final int THREAD_WAITING_TIME = 5000;
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtil.class);

	/**
	 * Sleep in give millis without throwing exception.
	 * 
	 * @param millis
	 *            milisecond.
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	/**
	 * Stop thread quietly.
	 * 
	 * @param thread
	 *            thread to be stop
	 * @param stopMaessage
	 *            message to be shown when stop thread forcely
	 */
	@SuppressWarnings("deprecation")
	public static void stopQuetly(Thread thread, String stopMaessage) {
		if (thread == null) {
			return;
		}
		// Wait 5000 second for natural death.
		try {
			thread.join(THREAD_WAITING_TIME);
		} catch (Exception e) {
			// Fall through
			noOp();
		}
		try {
			thread.interrupt();
		} catch (Exception e) {
			noOp();
		}
		try {
			// Again Wait 5000 second.
			thread.join(RETRY_MILLISECOND);
		} catch (Exception e) {
			// Fall through
			noOp();
		}
		try {
			// Force to Stop
			if (thread.isAlive()) {
				LOGGER.error(stopMaessage);
				thread.stop();
			}
		} catch (Exception e) {
			// Fall through
			noOp();
		}
	}

}
