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
package org.ngrinder.dns;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

/**
 * Local Dns Name Storage
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class NameStore {

	protected static NameStore singleton;

	protected Map<String, String> globalNames;

	protected NameStore() {
		globalNames = new ConcurrentHashMap<String, String>();
	}

	static {
		singleton = new NameStore();
		String hostPair = System.getProperty("ngrinder.etc.hosts");
		if (!StringUtils.isBlank(hostPair)) {
			String[] hostPairs = StringUtils.split(hostPair, ",");
			for (String pair : hostPairs) {
				String[] each = StringUtils.split(pair, ":");
				if (each.length != 2 || StringUtils.isEmpty(each[0]) || StringUtils.isEmpty(each[1])) {
					continue;
				}
				singleton.put(StringUtils.trim(each[1]), StringUtils.trim(each[0]));
			}
		}
	}

	public static NameStore getInstance() {
		return singleton;
	}

	public void put(String hostName, String ipAddress) {
		globalNames.put(hostName, ipAddress);
	}

	public void remove(String hostName) {
		globalNames.remove(hostName);
	}

	public String get(String hostName) {
		return globalNames.get(hostName);
	}

}
