package org.ngrinder.script.service;

import net.grinder.engine.agent.LocalScriptTestDriveService;

import java.io.File;

public class MockLocalScriptTestDriveService extends LocalScriptTestDriveService {

	public MockLocalScriptTestDriveService(File requiredLibraryDirectory) {
		super(requiredLibraryDirectory);
	}

	@Override
	protected int getDefaultTimeout() {
		return 10;
	}
}
