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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.infra.webhook.model.Event;
import org.ngrinder.infra.webhook.model.WebhookActivation;
import org.ngrinder.infra.webhook.model.WebhookConfig;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static java.time.Instant.now;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

	private final NGrinderWebhookClient ngrinderWebhookClient;

	private final WebhookActivationService webhookActivationService;

	private final ObjectMapper objectMapper;

	public void sendWebhookRequest(PerfTest perfTest,
								   WebhookConfig webhookConfig,
								   Event event) {

		Map<String, Object> payLoad = event.getPayloadBuilder().apply(perfTest);
		ResponseEntity<String> responseEntity = null;

		try {
			Mono<ResponseEntity<String>> responseEntityMono
				= ngrinderWebhookClient.post(webhookConfig.getPayloadUrl(), webhookConfig.getContentType().getMediaType(), payLoad);
			responseEntity = responseEntityMono.block(ofSeconds(2));
		} catch (RuntimeException e) {
			responseEntity = new ResponseEntity<>("An exception occurred while sending the webhook request.\n" + e.getMessage(), BAD_REQUEST);
		} finally {
			saveWebhookActivation(perfTest, payLoad, responseEntity);
		}
	}

	public void sendDummyWebhookRequest(User user, WebhookConfig webhookConfig, Event event) {
		PerfTest perfTest = new PerfTest();
		perfTest.setCreatedBy(user);
		sendWebhookRequest(perfTest, webhookConfig, event);
	}

	private void saveWebhookActivation(PerfTest perfTest,
									   Map<String, Object> request,
									   ResponseEntity<String> responseEntity) {

		Map<String, Object> response = newHashMap();
		if (responseEntity != null) {
			response.put("body", getSafe(responseEntity.getBody(), ""));
			response.put("header", getSafe(responseEntity.getHeaders(), ""));
			response.put("statusCode", responseEntity.getStatusCodeValue());
		}

		WebhookActivation webhookActivation = new WebhookActivation();
		webhookActivation.setCreatorId(perfTest.getCreatedBy().getUserId());
		webhookActivation.setUuid(UUID.randomUUID().toString());

		try {
			webhookActivation.setRequest(objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			webhookActivation.setRequest(request.toString());
		}

		try {
			webhookActivation.setResponse(objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			webhookActivation.setResponse(response.toString());
		}
		webhookActivation.setCreatedAt(now());
		webhookActivationService.save(webhookActivation);
	}

}
