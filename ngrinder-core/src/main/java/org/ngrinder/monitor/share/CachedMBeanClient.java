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
package org.ngrinder.monitor.share;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.ngrinder.monitor.share.domain.MBeanClient;

/**
 * 
 * Used to save the {@link MBeanClient} of monitor target.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class CachedMBeanClient {
	private static ConcurrentHashMap<String, MBeanClient> cache = new ConcurrentHashMap<String, MBeanClient>();

	/**
	 * Get {@link MBeanClient} of one target from the cache, if it doesn'r exist in cache, create a new
	 * one and put into cache.
	 * @param hostName is server name of monitor target
	 * @param port is the monitor listener of JMX on target
	 * @return MBeanClient of the target server
	 * @throws IOException
	 */
	public static MBeanClient getMBeanClient(String hostName, int port) throws IOException {
		final String key = getCacheKey(hostName, port);
		MBeanClient mc = cache.get(key);
		if (mc == null) {
			mc = new MBeanClient(hostName, port);
			cache.putIfAbsent(key, mc);
		}
		return mc;
	}

	private static String getCacheKey(String hostName, int port) {
		return (hostName == null ? "" : hostName) + "_" + port;
	}

}
