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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
// CHECKSTYLE:OFF
import sun.net.spi.nameservice.NameService;

/**
 * DNS Java DNS resolver.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class DNSJavaNameService implements NameService {

	/**
	 * Finds A records (ip addresses) for the host name.
	 * 
	 * @param name
	 *            host name to resolve.
	 * @return All the ip addresses found for the host name.
	 * @throws UnknownHostException
	 *             occurs when name is not available in DNS
	 */
	public InetAddress[] lookupAllHostAddr(String name) throws UnknownHostException {

		try {
			Record[] records = new Lookup(name, Type.A).run();
			if (records == null) {
				throw new UnknownHostException(name);
			}

			InetAddress[] array = new InetAddress[records.length];
			for (int i = 0; i < records.length; i++) {
				ARecord a = (ARecord) records[i];
				array[i] = a.getAddress();
			}

			return array;
		} catch (TextParseException e) {
			throw new UnknownHostException(e.getMessage());
		}
	}

	/**
	 * Finds PTR records (reverse dns lookups) for the ip address.
	 * 
	 * @param ip
	 *            ip address to lookup.
	 * @return The host name found for the ip address.
	 * @throws UnknownHostException
	 *             occurs when id is not available in DNS
	 */
	public String getHostByAddr(byte[] ip) throws UnknownHostException {

		try {
			String addr = DnsUtil.numericToTextFormat(ip);
			Record[] records = new Lookup(addr, Type.PTR).run();
			if (records == null) {
				throw new UnknownHostException(addr);
			}
			PTRRecord ptr = (PTRRecord) records[0];
			return ptr.getTarget().toString();
		} catch (TextParseException e) {
			throw new UnknownHostException(e.getMessage());
		}
	}
}
