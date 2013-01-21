package net.grinder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import net.grinder.common.GrinderException;

import org.junit.Test;
import org.ngrinder.infra.AgentConfig;

public class AgentControllerUpdateTest {
	@Test
	public void updateAgentController() throws GrinderException {
		AgentConfig agentConfig = mock(AgentConfig.class);
		File testDrive = new File("./src/test/resources/testdrive");
		AgentUpdateHandler agentController = new AgentUpdateHandler(agentConfig);
		testDrive.mkdirs();
		when(agentConfig.getCurrentDirectory()).thenReturn(testDrive);
		agentController.uncompress(new File(testDrive, "test_package.tar.gz"), new File(testDrive, "intemin"),
						new File(testDrive, "dest"));

	}
}
