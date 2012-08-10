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
package net.grinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControl.ProcessReports;

public final class ProcessControlListener implements ProcessControl.Listener {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessControlListener.class);

	private int lastWP = 0;

	public void update(ProcessReports[] processReports) {
		int wp = 0;

		for (int i = 0; i < processReports.length; ++i) {
			final WorkerProcessReport[] workerProcessStatuses = processReports[i].getWorkerProcessReports();
			wp += workerProcessStatuses.length;
		}
		if (lastWP != wp) {
			LOG.debug("Current worker process count:{}", wp);
		}
	}
}