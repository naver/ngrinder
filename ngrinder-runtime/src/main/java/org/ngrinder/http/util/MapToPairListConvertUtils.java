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
package org.ngrinder.http.util;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class MapToPairListConvertUtils {
	private MapToPairListConvertUtils() {
	}

	public static <R> List<R> convert(Map<String, String> map, BiFunction<String, String, R> converter) {
		return map.entrySet()
			.stream()
			.map(keyValueMapper(converter))
			.collect(toList());
	}

	private static <R> Function<Map.Entry<String, String>, R> keyValueMapper(BiFunction<String, String, R> biFunction) {
		return entry -> biFunction.apply(entry.getKey(), entry.getValue());
	}
}
