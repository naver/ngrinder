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

import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.ManagedConnPool;
import org.apache.hc.core5.pool.PoolEntry;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static net.grinder.util.NoOp.noOp;

public class ThreadAwareConnPool<T, C extends ModalCloseable> extends EmptyConnPoolControl<T, C> implements ManagedConnPool<T, C> {

	private final ThreadLocal<Set<PoolEntry<T, C>>> leased = ThreadLocal.withInitial(HashSet::new);
	private final ThreadLocal<LinkedList<PoolEntry<T, C>>> available = ThreadLocal.withInitial(LinkedList::new);

	@Override
	public Future<PoolEntry<T, C>> lease(T route, Object state, Timeout requestTimeout, FutureCallback<PoolEntry<T, C>> callback) {
		PoolEntry<T, C> poolEntry = this.getAvailable()
			.stream()
			.filter(entry -> entry.getRoute().equals(route))
			.findAny()
			.orElseGet(() -> new PoolEntry<>(route, Timeout.DISABLED));
		this.getLeased().add(poolEntry);

		BasicFuture<PoolEntry<T, C>> future = new BasicFuture<>(callback);
		future.completed(poolEntry);
		return future;
	}

	@Override
	public void release(PoolEntry<T, C> entry, boolean reusable) {
		if (entry == null) {
			return;
		}

		if (this.getLeased().remove(entry)) {
			final boolean keepAlive = entry.hasConnection() && reusable;
			if (keepAlive) {
				this.getAvailable().addFirst(entry);
			} else {
				entry.discardConnection(CloseMode.IMMEDIATE);
			}
		}
	}

	public void clear() {
		Stream<PoolEntry<T, C>> entryStream = Stream.concat(
			this.getLeased().stream(),
			this.getAvailable().stream()
		);
		entryStream.forEach(entry -> entry.discardConnection(CloseMode.IMMEDIATE));
		this.getLeased().clear();
		this.getAvailable().clear();
	}

	@Override
	public void close(CloseMode closeMode) {
		noOp();
	}

	@Override
	public void close() throws IOException {
		close(CloseMode.IMMEDIATE);
	}

	public Set<PoolEntry<T, C>> getLeased() {
		return leased.get();
	}

	public LinkedList<PoolEntry<T, C>> getAvailable() {
		return available.get();
	}
}
