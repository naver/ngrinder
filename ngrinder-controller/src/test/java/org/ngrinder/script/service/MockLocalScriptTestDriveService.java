package org.ngrinder.script.service;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("unit-test")
@Component
public class MockLocalScriptTestDriveService extends LocalScriptTestDriveService {
	@Override
	protected int getDefaultTimeout() {
		return 10;
	}
}
