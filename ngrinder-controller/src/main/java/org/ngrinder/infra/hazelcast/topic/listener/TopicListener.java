package org.ngrinder.infra.hazelcast.topic.listener;

import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;

/**
 * @since 3.5.0
 */
public interface TopicListener<T> {
	void execute(TopicEvent<T> event);
}
