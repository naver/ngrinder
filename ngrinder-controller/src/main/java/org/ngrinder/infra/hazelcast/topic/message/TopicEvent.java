package org.ngrinder.infra.hazelcast.topic.message;


import java.io.Serializable;

/**
 * @since 3.5.0
 */
public class TopicEvent<T> implements Serializable {
	private String type;
	private String key;
	private T data;

	public TopicEvent(String type, String key, T data) {
		this.type = type;
		this.key = key;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
