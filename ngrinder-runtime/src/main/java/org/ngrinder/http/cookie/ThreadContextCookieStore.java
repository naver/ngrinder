package org.ngrinder.http.cookie;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;

import java.util.Date;
import java.util.List;

public class ThreadContextCookieStore implements CookieStore {
	private static final ThreadLocal<CookieStore> cookieStoreThreadLocal = ThreadLocal.withInitial(BasicCookieStore::new);

	public static final ThreadContextCookieStore INSTANCE = new ThreadContextCookieStore();

	private ThreadContextCookieStore() {

	}

	@Override
	public void addCookie(Cookie cookie) {
		getCookieStore().addCookie(cookie);
	}

	@Override
	public List<Cookie> getCookies() {
		return getCookieStore().getCookies();
	}

	@Override
	public boolean clearExpired(Date date) {
		return getCookieStore().clearExpired(date);
	}

	@Override
	public void clear() {
		getCookieStore().clear();
	}

	public CookieStore getCookieStore() {
		return cookieStoreThreadLocal.get();
	}
}
