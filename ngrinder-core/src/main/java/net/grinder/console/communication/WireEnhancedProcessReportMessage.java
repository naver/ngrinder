package net.grinder.console.communication;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.message.console.AgentProcessPeformanceReportMessage;
import net.grinder.util.ReflectionUtil;

public class WireEnhancedProcessReportMessage {
	public WireEnhancedProcessReportMessage(ConsoleCommunication communication,
			ProcessControl processControl) {

		final MessageDispatchRegistry messageDispatch = communication
				.getMessageDispatchRegistry();
		final ProcessStatusImplementation m_processStatusSet = (ProcessStatusImplementation) ReflectionUtil
				.getFieldValue(processControl, "m_processStatusSet");
		messageDispatch.set(AgentProcessPeformanceReportMessage.class,
				new AbstractHandler<AgentProcessPeformanceReportMessage>() {
					public void handle(
							AgentProcessPeformanceReportMessage message)
							throws CommunicationException {
						m_processStatusSet.addAgentStatusReport(message);
						System.out.println(message.getPerformance().getCpu());
					}
				});
	}
}
