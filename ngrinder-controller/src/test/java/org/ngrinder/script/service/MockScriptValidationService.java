package org.ngrinder.script.service;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("unit-test")
@Component
public class MockScriptValidationService extends ScriptValidationService {
	public MockScriptValidationService(LocalScriptTestDriveService localScriptTestDriveService, FileEntryService fileEntryService, Config config, ScriptHandlerFactory scriptHandlerFactory) {
		super(localScriptTestDriveService, fileEntryService, config, scriptHandlerFactory);
	}

	@Override
	protected int getTimeout() {
		return 10;
	}
}
