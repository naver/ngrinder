package org.ngrinder.agent.service;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgentPackageServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentPackageService agentPackageService;

	@Test
	public void testIsDependentLib() {
		Set<String> libs = new HashSet<>();
		libs.add("ngrinder-groovy");
		libs.add("ngrinder-core");
		libs.add("ngrinder-runtime");
		libs.add("javassist");
		libs.add("jna");
		libs.add("jboss-transaction-api_1.2_spec");

		assertTrue(agentPackageService.isDependentLib(new File("ngrinder-core-3.5.1-SNAPSHOT.jar"), libs));
		assertTrue(agentPackageService.isDependentLib(new File("ngrinder-groovy-3.5.1.jar"), libs));
		assertTrue(agentPackageService.isDependentLib(new File("ngrinder-runtime-3.5.1-p4.jar"), libs));
		assertTrue(agentPackageService.isDependentLib(new File("javassist-3.24.0-GA.jar"), libs));
		assertTrue(agentPackageService.isDependentLib(new File("jna-5.6.0.jar"), libs));
		assertTrue(agentPackageService.isDependentLib(new File("jboss-transaction-api_1.2_spec-1.1.1.Final.jar"), libs));

		assertFalse(agentPackageService.isDependentLib(new File("jcommander-1.32.jar"), libs));
		assertFalse(agentPackageService.isDependentLib(new File("commons-collections-3.2.1.jar"), libs));
	}

}
