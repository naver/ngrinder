/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.impl.cookie;

import org.apache.hc.client5.http.cookie.*;
import org.apache.hc.client5.http.psl.PublicSuffixList;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps a {@link org.apache.hc.client5.http.cookie.CookieAttributeHandler} and leverages
 * its match method to never match a suffix from a black list. May be used to provide
 * additional security for cross-site attack types by preventing cookies from apparent
 * domains that are not publicly available.
 *
 *  @see PublicSuffixList
 *  @see PublicSuffixMatcher
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public class PublicSuffixDomainFilter implements CommonCookieAttributeHandler {

    private final CommonCookieAttributeHandler handler;
    private final PublicSuffixMatcher publicSuffixMatcher;
    private final Map<String, Boolean> localDomainMap;

    private static Map<String, Boolean> createLocalDomainMap() {
        final ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<>();
        map.put(".localhost.", Boolean.TRUE);  // RFC 6761
        map.put(".test.", Boolean.TRUE);       // RFC 6761
        map.put(".local.", Boolean.TRUE);      // RFC 6762
        map.put(".local", Boolean.TRUE);
        map.put(".localdomain", Boolean.TRUE);
        return map;
    }

    public PublicSuffixDomainFilter(
            final CommonCookieAttributeHandler handler, final PublicSuffixMatcher publicSuffixMatcher) {
        this.handler = Args.notNull(handler, "Cookie handler");
        this.publicSuffixMatcher = Args.notNull(publicSuffixMatcher, "Public suffix matcher");
        this.localDomainMap = createLocalDomainMap();
    }

    public PublicSuffixDomainFilter(
            final CommonCookieAttributeHandler handler, final PublicSuffixList suffixList) {
        Args.notNull(handler, "Cookie handler");
        Args.notNull(suffixList, "Public suffix list");
        this.handler = handler;
        this.publicSuffixMatcher = new PublicSuffixMatcher(suffixList.getRules(), suffixList.getExceptions());
        this.localDomainMap = createLocalDomainMap();
    }

    /**
     * Never matches if the cookie's domain is from the blacklist.
     */
    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        final String host = cookie.getDomain();
        if (host == null) {
            return false;
        }
        final int i = host.indexOf('.');
        if (i >= 0) {
            final String domain = host.substring(i);
            if (!this.localDomainMap.containsKey(domain)) {
                if (this.publicSuffixMatcher.matches(host)) {
                    return false;
                }
            }
        } else {
            if (!host.equalsIgnoreCase(origin.getHost())) {
                if (this.publicSuffixMatcher.matches(host)) {
                    return false;
                }
            }
        }
        return handler.match(cookie, origin);
    }

    @Override
    public void parse(final SetCookie cookie, final String value) throws MalformedCookieException {
        handler.parse(cookie, value);
    }

    @Override
    public void validate(final Cookie cookie, final CookieOrigin origin) throws MalformedCookieException {
        handler.validate(cookie, origin);
    }

    @Override
    public String getAttributeName() {
        return handler.getAttributeName();
    }

    public static CommonCookieAttributeHandler decorate(
            final CommonCookieAttributeHandler handler, final PublicSuffixMatcher publicSuffixMatcher) {
        Args.notNull(handler, "Cookie attribute handler");
        return publicSuffixMatcher != null ? new PublicSuffixDomainFilter(handler, publicSuffixMatcher) : handler;
    }

}
