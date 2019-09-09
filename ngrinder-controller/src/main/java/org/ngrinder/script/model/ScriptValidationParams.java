package org.ngrinder.script.model;

public class ScriptValidationParams {
	private FileEntry fileEntry;
	private String hostString;

	public ScriptValidationParams() {
	}

	public ScriptValidationParams(FileEntry fileEntry, String hostString) {
		this.fileEntry = fileEntry;
		this.hostString = hostString;
	}

	public FileEntry getFileEntry() {
		return fileEntry;
	}

	public void setFileEntry(FileEntry fileEntry) {
		this.fileEntry = fileEntry;
	}

	public String getHostString() {
		return hostString;
	}

	public void setHostString(String hostString) {
		this.hostString = hostString;
	}
}
