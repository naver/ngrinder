package org.ngrinder.http.cookie;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;

import java.util.List;

public class CookieManager {
	private static final CookieStore cookieStore = ThreadContextCookieStore.INSTANCE;

	public static void addCookies(List<Cookie> cookies) {
		cookies.forEach(cookieStore::addCookie);
	}

	public static void reset() {
		cookieStore.clear();
	}
}
