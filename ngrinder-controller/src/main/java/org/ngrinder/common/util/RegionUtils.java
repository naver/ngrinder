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
package org.ngrinder.common.util;

import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang.StringUtils.isEmpty;

@NoArgsConstructor(access = PRIVATE)
public final class RegionUtils {

	/**
	 * Convert configured string type of subregion value to {@link Set}.
	 *
	 * @param subregionsString value of 'ngrinder.cluster.subregion' in system-ex.conf, it concatenates with ',' like sub1,sub2
	 */
	public static Set<String> convertSubregionsStringToSet(String subregionsString) {
		return isEmpty(subregionsString) ? emptySet() : new HashSet<>(asList(subregionsString.split(",")));
	}

}
