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
package org.ngrinder.http;

import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class CookieManager {
	private static final ThreadLocal<List<Cookie>> cookieJar = ThreadLocal.withInitial(ArrayList::new);

	public static void addCookies(List<Cookie> list) {
		synchronized (cookieJar) {
			cookieJar.set(merge(cookieJar.get(), list));
		}
	}

	public static List<Cookie> getCookies() {
		return cookieJar.get();
	}

	private static List<Cookie> merge(List<Cookie> origin, List<Cookie> cookies) {
		Predicate<Cookie> sameCookieFilter = originCookie ->
			cookies.stream().anyMatch(cookie -> !cookie.name().equalsIgnoreCase(originCookie.name()));

		List<Cookie> merged = origin.stream()
			.filter(sameCookieFilter)
			.collect(toList());
		merged.addAll(cookies);

		return merged;
	}
}
