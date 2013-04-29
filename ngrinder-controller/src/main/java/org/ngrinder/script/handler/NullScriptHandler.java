package org.ngrinder.script.handler;

import java.io.File;

import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

@Component
public class NullScriptHandler extends ScriptHandler {

	public NullScriptHandler() {
		super("", null, "plain");
	}

	@Override
	public Integer order() {
		return 500;
	}

	@Override
	public void prepareDist(String identifier, User user, FileEntry script, File distDir, PropertiesWrapper properties) {

	}

	@Override
	public boolean canHandle(FileEntry fileEntry) {
		return true;
	}

	@Override
	public String checkSyntaxErrors(String content) {
		return null;
	}
}