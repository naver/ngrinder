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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
public class NGrinderWebhookClient {

	private static final int REQUEST_TIME_OUT_SECOND = 5;

	private final WebClient webClient;

	Mono<ResponseEntity<String>> post(String url,
									  MediaType mediaType,
									  Map<String, Object> payLoad,
									  Consumer<HttpHeaders> headers) {

		Object payloadBody = payLoad;

		if (mediaType.equals(APPLICATION_FORM_URLENCODED)) {
			payloadBody = mapToQueryString(payLoad);
		}

		return requireNonNull(webClient
			.post()
			.uri(url)
			.contentType(mediaType)
			.headers(headers)
			.body(fromValue(payloadBody))
			.exchange()
			.block(ofSeconds(REQUEST_TIME_OUT_SECOND)))
			.toEntity(String.class);
	}

	/**
	 * Convert {@link Map} to query string.
	 * */
	private String mapToQueryString(Map<String, Object> params) {
		return params.entrySet().stream()
			.map(param -> param.getKey() + "=" + param.getValue())
			.reduce((param1, param2) -> param1 + "&" + param2)
			.orElse("");
	}
}
