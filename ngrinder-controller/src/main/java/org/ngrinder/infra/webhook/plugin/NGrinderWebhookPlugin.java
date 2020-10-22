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
package org.ngrinder.infra.webhook.plugin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.extension.OnTestLifeCycleRunnable;
import org.ngrinder.infra.webhook.model.Event;
import org.ngrinder.infra.webhook.model.WebhookConfig;
import org.ngrinder.infra.webhook.service.WebhookConfigService;
import org.ngrinder.infra.webhook.service.WebhookService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.service.IPerfTestService;

import static org.ngrinder.infra.webhook.model.Event.FINISH;
import static org.ngrinder.infra.webhook.model.Event.START;

/**
 * nGrinder webhook plugin.
 *
 * @since 3.5.2
 */
@Slf4j
@AllArgsConstructor
public class NGrinderWebhookPlugin implements OnTestLifeCycleRunnable {

	// Be injected manually for backward compatibility.
	private WebhookService webhookService;

	private WebhookConfigService webhookConfigService;

	@Override
	public void start(PerfTest perfTest, IPerfTestService perfTestService, String version) {
		try {
			sendWebhookRequest(perfTest, START);
		} catch (RuntimeException e) {
			log.error("[{}] An exception occurred while sending the webhook start request.", perfTest.getId(), e);
		}
	}

	@Override
	public void finish(PerfTest perfTest, String stopReason, IPerfTestService perfTestService, String version) {
		try {
			sendWebhookRequest(perfTest, FINISH);
		} catch (RuntimeException e) {
			log.error("[{}] An exception occurred while sending the webhook finish request.", perfTest.getId(), e);
		}
	}

	private void sendWebhookRequest(PerfTest perfTest, Event event) {
		WebhookConfig webhookConfig = webhookConfigService.getOne(perfTest.getCreatedBy().getUserId());

		if (webhookConfig == null) {
			return;
		}

		if (webhookConfig.isActive() && webhookConfig.getEvents().contains(event)) {
			webhookService.sendWebhookRequest(perfTest, webhookConfig, event);
		}
	}

}
