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

import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.TimeValue;

import java.util.Set;

public abstract class EmptyConnPoolControl<T, C extends ModalCloseable> implements ConnPoolControl<T> {

	@Override
	public void setMaxTotal(int max) {
		// do nothing
	}

	@Override
	public int getMaxTotal() {
		return 0;
	}

	@Override
	public void setDefaultMaxPerRoute(int max) {
		// do nothing
	}

	@Override
	public int getDefaultMaxPerRoute() {
		return 0;
	}

	@Override
	public void setMaxPerRoute(T route, int max) {
		// do nothing
	}

	@Override
	public int getMaxPerRoute(T route) {
		return 0;
	}

	@Override
	public void closeIdle(TimeValue idleTime) {
		// do nothing
	}

	@Override
	public void closeExpired() {
		// do nothing
	}

	@Override
	public Set<T> getRoutes() {
		return null;
	}

	@Override
	public PoolStats getTotalStats() {
		return null;
	}

	@Override
	public PoolStats getStats(T route) {
		return null;
	}
}
