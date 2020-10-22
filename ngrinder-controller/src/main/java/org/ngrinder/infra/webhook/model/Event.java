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
import org.ngrinder.model.Status;
import org.ngrinder.model.User;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import static java.time.Instant.now;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.model.Status.UNKNOWN;

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
		payload.put("eventType", "START");
		payload.put("startTime", now());
		return payload;
	}),

	FINISH(finishedPerfTest -> {
		Map<String, Object> payload = createBasePayload(finishedPerfTest);

		long errors = getSafe(finishedPerfTest.getErrors(), 0L);
		long run = getSafe(finishedPerfTest.getRunCount(), 0);
		Status status = getSafe(finishedPerfTest.getStatus(), UNKNOWN);

		payload.put("eventType", "FINISH");
		payload.put("finishTime", now());
		payload.put("peakTPS", getSafe(finishedPerfTest.getPeakTps(), 0.0));
		payload.put("TPS", getSafe(finishedPerfTest.getTps(), 0.0));
		payload.put("errors", errors);
		payload.put("executedTests", run);
		payload.put("successfulTests", run - errors);
		payload.put("status", getSafe(status.getName(), ""));
		payload.put("meanTestTime", getSafe(finishedPerfTest.getMeanTestTime(), 0.0));
		payload.put("runTime", getSafe(finishedPerfTest.getRuntimeStr(), "0"));

		return payload;
	});

	private final Function<PerfTest, Map<String, Object>> payloadBuilder;

	private static Map<String, Object> createBasePayload(PerfTest perfTest) {
		Map<String, Object> payload = newHashMap();

		int vuserPerAgent = getSafe(perfTest.getVuserPerAgent(), 0);
		int agentCount = getSafe(perfTest.getAgentCount(), 0);
		User createdBy = getSafe(perfTest.getCreatedBy(), new User());

		payload.put("createdBy", getSafe(createdBy.getUserId(), ""));
		payload.put("testId", getSafe(perfTest.getId(), 0L));
		payload.put("testName", getSafe(perfTest.getTestName(), ""));
		payload.put("scriptName", getSafe(perfTest.getScriptName(), ""));
		payload.put("vuser", vuserPerAgent * agentCount);
		payload.put("tags", getSafe(perfTest.getTagString(), ""));

		return payload;
	}
}
