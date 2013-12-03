package org.ngrinder.script.service;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import org.ngrinder.infra.annotation.TestOnlyComponent;

@TestOnlyComponent
public class MockLocalScriptTestDriveService extends LocalScriptTestDriveService {
	@Override
	protected int getDefaultTimeout() {
		return 10;
	}
}
