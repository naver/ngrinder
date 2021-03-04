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
package org.apache.hc.client5.http.cookie;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default implementation of {@link CookieStore}
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class BasicCookieStore implements CookieStore, Serializable {

    private static final long serialVersionUID = -7581093305228232025L;

    private final TreeSet<Cookie> cookies;
    private transient ReadWriteLock lock;

    public BasicCookieStore() {
        super();
        this.cookies = new TreeSet<>(new CookieIdentityComparator());
        this.lock = new ReentrantReadWriteLock();
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        /* Reinstantiate transient fields. */
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent cookies.
     * If the given cookie has already expired it will not be added, but existing
     * values will still be removed.
     *
     * @param cookie the {@link Cookie cookie} to be added
     *
     * @see #addCookies(Cookie[])
     *
     */
    @Override
    public void addCookie(final Cookie cookie) {
        if (cookie != null) {
            lock.writeLock().lock();
            try {
                // first remove any old cookie that is equivalent
                cookies.remove(cookie);
                if (!cookie.isExpired(new Date())) {
                    cookies.add(cookie);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * Adds an array of {@link Cookie HTTP cookies}. Cookies are added individually and
     * in the given array order. If any of the given cookies has already expired it will
     * not be added, but existing values will still be removed.
     *
     * @param cookies the {@link Cookie cookies} to be added
     *
     * @see #addCookie(Cookie)
     *
     */
    public void addCookies(final Cookie[] cookies) {
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                this.addCookie(cookie);
            }
        }
    }

    /**
     * Returns an immutable array of {@link Cookie cookies} that this HTTP
     * state currently contains.
     *
     * @return an array of {@link Cookie cookies}.
     */
    @Override
    public List<Cookie> getCookies() {
        lock.readLock().lock();
        try {
            //create defensive copy so it won't be concurrently modified
            return new ArrayList<>(cookies);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes all of {@link Cookie cookies} in this HTTP state
     * that have expired by the specified {@link Date date}.
     *
     * @return true if any cookies were purged.
     *
     * @see Cookie#isExpired(Date)
     */
    @Override
    public boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        lock.writeLock().lock();
        try {
            boolean removed = false;
            for (final Iterator<Cookie> it = cookies.iterator(); it.hasNext(); ) {
                if (it.next().isExpired(date)) {
                    it.remove();
                    removed = true;
                }
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all cookies.
     */
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            cookies.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return cookies.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

}
