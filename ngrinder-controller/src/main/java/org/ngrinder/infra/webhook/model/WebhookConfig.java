/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.infra.webhook.model;

import lombok.*;
import org.ngrinder.model.BaseEntity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;

import static java.time.Instant.now;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.ngrinder.common.util.CollectionUtils.newHashSet;

/**
 * Webhook configuration model.
 *
 * @since 3.5.2
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WEBHOOK_CONFIG")
public class WebhookConfig extends BaseEntity<WebhookConfig> {

	@Column(name = "created_user_id")
	private String createdUserId;

	private boolean active;

	@Column(name = "payload_url")
	private String payloadUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "content_type")
	private ContentType contentType;

	private String events;

	@Column(name = "created_time")
	private Instant createdTime;

	@Column(name = "last_modified_time")
	private Instant lastModifiedTime;

	@Transient
	@Getter(value = AccessLevel.NONE)
	private Set<Event> eventSet = newHashSet();

	@PreUpdate
	public void preUpdate() {
		this.lastModifiedTime = now();
	}

	public Set<Event> getEvents() {
		if (isEmpty(events)) {
			return eventSet;
		}

		if (eventSet.isEmpty()) {
			String[] split = events.split(",");
			for (String value : split) {
				eventSet.add(Event.valueOf(value));
			}
		}

		return eventSet;
	}

	// Using at hibernate dirty object.
	public void update(WebhookConfig webhookConfig) {
		this.contentType = webhookConfig.contentType;
		this.events = webhookConfig.events;
		this.payloadUrl = webhookConfig.payloadUrl;
		this.active = webhookConfig.active;
	}
}
