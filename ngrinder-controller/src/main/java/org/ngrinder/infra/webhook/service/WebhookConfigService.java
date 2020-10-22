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
package org.ngrinder.infra.webhook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.infra.webhook.model.WebhookConfig;
import org.ngrinder.infra.webhook.repository.WebhookConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.time.Instant.now;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.infra.webhook.repository.WebhookConfigSpecification.creatorIdEqual;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookConfigService {

	private final WebhookConfigRepository webhookConfigRepository;

	@Transactional
	public void save(WebhookConfig webhookConfig) {
		checkNotNull(webhookConfig);
		Optional<WebhookConfig> findOne = webhookConfigRepository.findOne(creatorIdEqual(webhookConfig.getCreatorId()));
		if (findOne.isPresent()) {
			WebhookConfig existingWebhookConfig = findOne.get();
			existingWebhookConfig.update(webhookConfig);
			return;
		}
		webhookConfig.setCreatedAt(now());
		webhookConfigRepository.save(webhookConfig);
	}

	public WebhookConfig getOne(String createdBy) {
		Optional<WebhookConfig> webhookConfig = webhookConfigRepository.findOne(creatorIdEqual(createdBy));
		return webhookConfig.orElse(null);
	}
}
