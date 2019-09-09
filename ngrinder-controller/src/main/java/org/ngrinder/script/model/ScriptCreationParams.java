package org.ngrinder.script.model;

public class ScriptCreationParams {
	private String FileName;
	private String testUrl;
	private String options;
	private String scriptType;
	private boolean createLibAndResource;

	public ScriptCreationParams() {

	}

	public ScriptCreationParams(String fileName, String testUrl, String options, String scriptType, boolean createLibAndResource) {
		FileName = fileName;
		this.testUrl = testUrl;
		this.options = options;
		this.scriptType = scriptType;
		this.createLibAndResource = createLibAndResource;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getTestUrl() {
		return testUrl;
	}

	public void setTestUrl(String testUrl) {
		this.testUrl = testUrl;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public String getScriptType() {
		return scriptType;
	}

	public void setScriptType(String scriptType) {
		this.scriptType = scriptType;
	}

	public boolean isCreateLibAndResource() {
		return createLibAndResource;
	}

	public void setCreateLibAndResource(boolean createLibAndResource) {
		this.createLibAndResource = createLibAndResource;
	}
}
