package org.ngrinder.infra.hazelcast.topic.subscriber;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.ngrinder.infra.hazelcast.topic.listener.TopicListener;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 3.5.0
 */
@SuppressWarnings({"unused", "unchecked"})
public class TopicSubscriber implements MessageListener<TopicEvent> {

	private Map<String, TopicListener> listenerMap = new ConcurrentHashMap<>();

	@Override
	public void onMessage(Message<TopicEvent> message) {
		TopicEvent event = message.getMessageObject();
		if (listenerMap.containsKey(event.getType())) {
			listenerMap.get(event.getType()).execute(event);
		}
	}

	public void addListener(String type, TopicListener listener) {
		this.listenerMap.put(type, listener);
	}

	public void removeListener(String type) {
		listenerMap.remove(type);
	}
}
