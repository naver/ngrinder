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
package org.ngrinder.infra.webhook.controller;

import lombok.RequiredArgsConstructor;
import org.ngrinder.infra.webhook.model.WebhookActivation;
import org.ngrinder.infra.webhook.model.WebhookConfig;
import org.ngrinder.infra.webhook.service.WebhookActivationService;
import org.ngrinder.infra.webhook.service.WebhookConfigService;
import org.ngrinder.infra.webhook.service.WebhookService;
import org.ngrinder.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ngrinder.infra.webhook.model.Event.FINISH;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/api")
public class WebhookApiController {

	private final WebhookConfigService webhookConfigService;

	private final WebhookService webhookService;

	private final WebhookActivationService webhookActivationService;

	@PostMapping
	public void save(@RequestBody WebhookConfig webhookConfig) {
		webhookConfigService.save(webhookConfig);
	}

	@PostMapping("/validate")
	public void validatePayloadUrl(User user, @RequestBody WebhookConfig webhookConfig) {
		webhookService.sendDummyWebhookRequest(user, webhookConfig, FINISH);
	}

	@GetMapping
	public WebhookConfig getOne(User user) {
		return webhookConfigService.getOne(user.getUserId());
	}

	@GetMapping("/activation")
	public List<WebhookActivation> getActivations(@RequestParam String creatorId,
												  @PageableDefault Pageable pageable) {
		return webhookActivationService.findAll(creatorId, pageable);
	}
}
