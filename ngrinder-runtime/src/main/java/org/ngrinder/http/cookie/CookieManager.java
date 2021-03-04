package org.ngrinder.http.cookie;

import org.apache.hc.client5.http.cookie.CookieStore;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CookieManager {
	private static final CookieStore cookieStore = ThreadContextCookieStore.INSTANCE;

	public static void addCookies(List<Cookie> cookies) {
		cookies.forEach(CookieManager::addCookie);
	}

	public static void addCookie(Cookie cookie) {
		cookieStore.addCookie(cookie.getRealCookie());
	}

	public static List<Cookie> getCookies() {
		return cookieStore.getCookies()
			.stream()
			.map(Cookie::from)
			.collect(toList());
	}

	public static void reset() {
		cookieStore.clear();
	}
}
