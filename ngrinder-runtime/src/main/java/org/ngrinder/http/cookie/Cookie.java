package org.ngrinder.http.cookie;

import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import java.time.Instant;
import java.util.Date;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Cookie {
	private final BasicClientCookie realCookie;

	public Cookie(String name, String value) {
		realCookie = new BasicClientCookie(name, value);
	}

	public Cookie(String name, String value, String domain, String path, int expire) {
		realCookie = new BasicClientCookie(name, value);
		realCookie.setDomain(domain);
		realCookie.setPath(path);
		if (expire >= 0) {
			realCookie.setExpiryDate(Date.from(Instant.now().plus(expire, SECONDS)));
		}
	}

	public static Cookie from(org.apache.hc.client5.http.cookie.Cookie realCookie) {
		Cookie cookie = new Cookie(realCookie.getName(), realCookie.getValue());
		cookie.realCookie.setDomain(realCookie.getDomain());
		cookie.realCookie.setPath(realCookie.getPath());
		cookie.realCookie.setExpiryDate(realCookie.getExpiryDate());
		return cookie;
	}

	BasicClientCookie getRealCookie() {
		return realCookie;
	}

	@Override
	public String toString() {
		return "Cookie(" +
			"name: " + realCookie.getName() +
			", value: " + realCookie.getValue() +
			", domain: " + realCookie.getDomain() +
			", path: " + realCookie.getPath() +
			", expire: " + realCookie.getExpiryDate() + ")";
	}
}
