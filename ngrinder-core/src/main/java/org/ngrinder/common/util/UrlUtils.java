package org.ngrinder.common.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

public class UrlUtils {
	/**
	 * Get host part of the given url.
	 * 
	 * @param url
	 *            url
	 * @return host name
	 */
	public static String getHost(String url) {
		try {
			return StringUtils.trim(new URL(url).getHost());
		} catch (MalformedURLException e) {
			return "";
		}
	}
}
