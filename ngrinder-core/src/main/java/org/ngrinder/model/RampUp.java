package org.ngrinder.model;

public enum RampUp {
	PROCESS("process"),
	THREAD("thread");
	private String messageKey;

	RampUp(String messageKey) {
		this.messageKey = messageKey;
	}

	public String getMessageKey() {
		return messageKey;
	}
}
