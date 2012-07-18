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
package net.grinder.console.communication;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.message.console.AgentProcessPeformanceReportMessage;
import net.grinder.util.ReflectionUtil;

/**
 * Wire ProcessReportMessage from agent to {@link ProcessControl} instance.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class WireEnhancedProcessReportMessage {
	/**
	 * Constructor.
	 * 
	 * @param communication
	 *            {@link ConsoleCommunication} instance from which messages come
	 * @param processControl
	 *            {@link ProcessControl} instance to which the agent status is
	 *            updated
	 */
	public WireEnhancedProcessReportMessage(ConsoleCommunication communication, ProcessControl processControl) {

		final MessageDispatchRegistry messageDispatch = communication.getMessageDispatchRegistry();
		final ProcessStatusImplementation m_processStatusSet = (ProcessStatusImplementation) ReflectionUtil
				.getFieldValue(processControl, "m_processStatusSet");
		messageDispatch.set(AgentProcessPeformanceReportMessage.class,
				new AbstractHandler<AgentProcessPeformanceReportMessage>() {
					public void handle(AgentProcessPeformanceReportMessage message) throws CommunicationException {
						m_processStatusSet.addAgentStatusReport(message);
					}
				});
	}
}
