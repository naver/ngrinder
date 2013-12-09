package org.ngrinder.script.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("unit-test")
@Component
public class MockScriptValidationService extends ScriptValidationService {
	@Override
	protected int getTimeout() {
		return 10;
	}
}
