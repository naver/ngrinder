package org.ngrinder.script.service;

import org.ngrinder.infra.annotation.TestOnlyComponent;

@TestOnlyComponent
public class MockScriptValidationService extends ScriptValidationService {
	@Override
	protected int getTimeout() {
		return 10;
	}
}
