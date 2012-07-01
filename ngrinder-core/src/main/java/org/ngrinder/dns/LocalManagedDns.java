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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

import sun.net.spi.nameservice.NameService;

/**
 * Locally managed DNS
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class LocalManagedDns implements NameService {

	NameService defaultDnsImpl = new DNSJavaNameService();

	/**
	 * @see sun.net.spi.nameservice.NameService#getHostByAddr(byte[])
	 */
	public String getHostByAddr(byte[] ip) throws UnknownHostException {
		return defaultDnsImpl.getHostByAddr(ip);
	}

	/**
	 * @see sun.net.spi.nameservice.NameService#lookupAllHostAddr(java.lang.String)
	 */
	public InetAddress[] lookupAllHostAddr(String name) throws UnknownHostException {

		String ipAddress = NameStore.getInstance().get(name);
		if (StringUtils.isNotEmpty(ipAddress)) {
			InetAddress address = Inet4Address.getByAddress(DnsUtil.textToNumericFormat(ipAddress));
			return new InetAddress[] { address };
		} else {
			return defaultDnsImpl.lookupAllHostAddr(name);
		}
	}

}
