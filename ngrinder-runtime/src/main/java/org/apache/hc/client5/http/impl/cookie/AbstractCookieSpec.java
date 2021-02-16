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

import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.CookieAttributeHandler;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Asserts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract cookie specification which can delegate the job of parsing,
 * validation or matching cookie attributes to a number of arbitrary
 * {@link CookieAttributeHandler}s.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.SAFE)
public abstract class AbstractCookieSpec implements CookieSpec {

    /**
    * Stores attribute name -> attribute handler mappings
    */
    private final Map<String, CookieAttributeHandler> attribHandlerMap;

    /**
     * Default constructor
     * */
    public AbstractCookieSpec() {
        super();
        this.attribHandlerMap = new ConcurrentHashMap<>(10);
    }

    /**
     * @since 4.4
     */
    protected AbstractCookieSpec(final HashMap<String, CookieAttributeHandler> map) {
        super();
        Asserts.notNull(map, "Attribute handler map");
        this.attribHandlerMap = new ConcurrentHashMap<>(map);
    }

    /**
     * @since 4.4
     */
    protected AbstractCookieSpec(final CommonCookieAttributeHandler... handlers) {
        super();
        this.attribHandlerMap = new ConcurrentHashMap<>(handlers.length);
        for (final CommonCookieAttributeHandler handler: handlers) {
            this.attribHandlerMap.put(handler.getAttributeName(), handler);
        }
    }

    /**
     * Finds an attribute handler {@link CookieAttributeHandler} for the
     * given attribute. Returns {@code null} if no attribute handler is
     * found for the specified attribute.
     *
     * @param name attribute name. e.g. Domain, Path, etc.
     * @return an attribute handler or {@code null}
     */
    protected CookieAttributeHandler findAttribHandler(final String name) {
        return this.attribHandlerMap.get(name);
    }

    /**
     * Gets attribute handler {@link CookieAttributeHandler} for the
     * given attribute.
     *
     * @param name attribute name. e.g. Domain, Path, etc.
     * @throws IllegalStateException if handler not found for the
     *          specified attribute.
     */
    protected CookieAttributeHandler getAttribHandler(final String name) {
        final CookieAttributeHandler handler = findAttribHandler(name);
        Asserts.check(handler != null, "Handler not registered for " +
                name + " attribute");
        return handler;
    }

    protected Collection<CookieAttributeHandler> getAttribHandlers() {
        return this.attribHandlerMap.values();
    }

}
