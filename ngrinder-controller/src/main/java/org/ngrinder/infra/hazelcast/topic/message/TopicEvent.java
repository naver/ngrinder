package org.ngrinder.infra.hazelcast.topic.message;


import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @since 3.5.0
 */
@Getter
@Setter
public class TopicEvent<T> implements Serializable {
	private String type;
	private String key;
	private T data;

	public TopicEvent(String type, String key, T data) {
		this.type = type;
		this.key = key;
		this.data = data;
	}
}
