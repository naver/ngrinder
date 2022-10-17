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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.springframework.http.MediaType.*;

/**
 * Supported webhook Content-Type.
 *
 * @since 3.5.2
 */
@Getter
@JsonDeserialize(using = ContentType.ContentTypeDeserializer.class)
@AllArgsConstructor
public enum ContentType {

	JSON("application/json", APPLICATION_JSON),
	FORM_URL_ENCODED("application/x-www-form-urlencoded", APPLICATION_FORM_URLENCODED);

	private final String name;

	private final MediaType mediaType;

	public static class ContentTypeDeserializer extends JsonDeserializer<ContentType> {
		@Override
		public ContentType deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
			JsonNode node = parser.getCodec().readTree(parser);
			try {
				return ContentType.valueOf(node.asText());
			} catch (IllegalArgumentException e) {
				if (node.get("contentType") != null) {
					return ContentType.valueOf(node.get("contentType").asText());
				} else {
					throw new NGrinderRuntimeException("ContentType must present. you can use 'contentType' for data key");
				}
			}
		}
	}
}

