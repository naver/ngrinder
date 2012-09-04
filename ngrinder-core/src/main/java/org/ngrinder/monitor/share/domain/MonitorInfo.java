/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.monitor.share.domain;

import javax.management.openmbean.CompositeData;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class MonitorInfo {
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	protected long collectTime;

	// public abstract MonitorInfo from(CompositeData cd);

	public abstract void parse(CompositeData cd);

	protected static String getString(CompositeData cd, String itemName) {
		if (cd == null)
			throw new IllegalArgumentException("Null CompositeData");

		return (String) cd.get(itemName);
	}

	protected static long getLong(CompositeData cd, String itemName) {
		if (cd == null)
			throw new IllegalArgumentException("Null CompositeData");

		return ((Long) cd.get(itemName)).longValue();
	}

	protected static int getInt(CompositeData cd, String itemName) {
		if (cd == null)
			throw new IllegalArgumentException("Null CompositeData");

		return ((Integer) cd.get(itemName)).intValue();
	}

	protected static float getFloat(CompositeData cd, String itemName) {
		if (cd == null)
			throw new IllegalArgumentException("Null CompositeData");

		return ((Float) cd.get(itemName)).floatValue();
	}

	public long getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
}
