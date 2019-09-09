package org.ngrinder.script.model;

public class FileSaveParams {
	private FileEntry fileEntry;
	private String targetHosts;
	private String validated = "0";
	private boolean createLibAndResource;

	public FileSaveParams() {

	}

	public FileSaveParams(FileEntry fileEntry, String targetHosts, String validated, boolean createLibAndResource) {
		this.fileEntry = fileEntry;
		this.createLibAndResource = createLibAndResource;
		this.targetHosts = targetHosts;
		this.validated = validated;
	}

	public FileEntry getFileEntry() {
		return fileEntry;
	}

	public void setFileEntry(FileEntry fileEntry) {
		this.fileEntry = fileEntry;
	}

	public boolean isCreateLibAndResource() {
		return createLibAndResource;
	}

	public void setCreateLibAndResource(boolean createLibAndResource) {
		this.createLibAndResource = createLibAndResource;
	}

	public String getTargetHosts() {
		return targetHosts;
	}

	public void setTargetHosts(String targetHosts) {
		this.targetHosts = targetHosts;
	}

	public String getValidated() {
		return validated;
	}

	public void setValidated(String validated) {
		this.validated = validated;
	}
}
