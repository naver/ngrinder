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
import org.ngrinder.infra.webhook.model.WebhookActivation;
import org.ngrinder.infra.webhook.repository.WebhookActivationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.infra.webhook.repository.WebhookActivationSpecification.creatorIdEqual;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookActivationService {

	private final WebhookActivationRepository webhookActivationRepository;

	public List<WebhookActivation> findAll(String creatorId, Pageable pageable) {
		Page<WebhookActivation> webhookActivation = webhookActivationRepository.findAll(creatorIdEqual(creatorId), pageable);
		return webhookActivation.getContent();
	}

	public void save(WebhookActivation webhookActivation) {
		checkNotNull(webhookActivation);
		webhookActivationRepository.save(webhookActivation);
	}
}
