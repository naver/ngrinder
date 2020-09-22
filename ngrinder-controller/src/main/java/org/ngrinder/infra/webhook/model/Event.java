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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ngrinder.model.PerfTest;

import java.util.Map;
import java.util.function.Function;

import static java.time.LocalDateTime.now;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;

/**
 * Webhook event.
 *
 * @since 3.5.2
 */
@Getter
@AllArgsConstructor
public enum Event {

	START(perfTest -> {
		Map<String, Object> payload = createBasePayload(perfTest);
		payload.put("startTime", now().toString());
		return payload;
	}),

	FINISH(finishedTest -> {
		Map<String, Object> payload = createBasePayload(finishedTest);

		long errors = finishedTest.getErrors();
		long run = finishedTest.getRunCount();

		payload.put("finishTime",  now().toString());
		payload.put("peakTPS", finishedTest.getPeakTps());
		payload.put("TPS", finishedTest.getTps());
		payload.put("errors", errors);
		payload.put("executedTests", run);
		payload.put("successfulTests", run - errors);
		payload.put("status", finishedTest.getStatus().getName());
		payload.put("meanTestTime", finishedTest.getMeanTestTime());
		payload.put("runTime", finishedTest.getRuntimeStr());
		return payload;
	});

	private Function<PerfTest, Map<String, Object>> payloadBuilder;
	
	private static Map<String, Object> createBasePayload(PerfTest perfTest) {
		Map<String, Object> payload = newHashMap();
		payload.put("createdUserId", perfTest.getCreatedUser().getUserId());
		payload.put("testId", perfTest.getId());
		payload.put("scriptName", perfTest.getScriptName());
		payload.put("vuser", perfTest.getVuserPerAgent() * perfTest.getAgentCount());
		return payload;
	}
}
