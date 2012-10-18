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
package org.ngrinder.monitor.agent;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ngrinder.monitor.agent.mxbean.core.MXBean;

/**
 * 
 * Used to store monitor MXBean in a map, with the domain name as the key.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class AgentMXBeanStorage {
	private Map<String, MXBean> cachedMxBeans = new ConcurrentHashMap<String, MXBean>();
	private static final AgentMXBeanStorage INSTANCE = new AgentMXBeanStorage();

	private AgentMXBeanStorage() {
	}

	public int getSize() {
		return cachedMxBeans.size();
	}

	public static AgentMXBeanStorage getInstance() {
		return INSTANCE;
	}

	/**
	 * get the monitor MXBean from the storage.
	 * @param key is the domain name of JMX
	 * @return MXBean registered with this key
	 */
	public MXBean getMXBean(String key) {
		return cachedMxBeans.get(key);
	}

	/**
	 * Add the monitor MXBean into the storage, with the domain name as the key.
	 * @param key is the domain name of JMX
	 * @param mxBean is the monitor MXBean
	 */
	public void addMXBean(String key, MXBean mxBean) {
		cachedMxBeans.put(key, mxBean);
	}

	public Collection<MXBean> getMXBeans() {
		return cachedMxBeans.values();
	}
}
