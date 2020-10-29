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
package org.ngrinder.infra.webhook.repository;

import org.ngrinder.infra.webhook.model.WebhookConfig;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link WebhookConfig} Specification for the more elaborated Webhook query.
 *
 * @since 3.5.2
 */
public abstract class WebhookConfigSpecification {

	public static Specification<WebhookConfig> creatorIdEqual(String creatorId) {
		return (Specification<WebhookConfig>) (root, query, cb) -> cb.equal(root.get("creatorId"), creatorId);
	}
}
