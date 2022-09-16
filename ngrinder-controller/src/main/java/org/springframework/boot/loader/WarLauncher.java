package org.springframework.boot.loader;

import org.springframework.boot.loader.archive.Archive;

public class WarLauncher extends ExecutableArchiveLauncher {

	public WarLauncher() {
		System.out.println("Set tmp directory path to \"/Users/imb/.ngrinder/tmp\"");
		System.setProperty("java.io.tmpdir", "/Users/imb/.ngrinder/tmp");
	}

	protected WarLauncher(Archive archive) {
		super(archive);
		System.out.println("Set tmp directory path to \"/Users/imb/.ngrinder/tmp\"");
		System.setProperty("java.io.tmpdir", "/Users/imb/.ngrinder/tmp");
	}

	@Override
	protected boolean isPostProcessingClassPathArchives() {
		return false;
	}

	@Override
	protected boolean isSearchCandidate(Archive.Entry entry) {
		return entry.getName().startsWith("WEB-INF/");
	}

	@Override
	public boolean isNestedArchive(Archive.Entry entry) {
		if (entry.isDirectory()) {
			return entry.getName().equals("WEB-INF/classes/");
		}
		return entry.getName().startsWith("WEB-INF/lib/") || entry.getName().startsWith("WEB-INF/lib-provided/");
	}

	public static void main(String[] args) throws Exception {
		new WarLauncher().launch(args);
	}

}
