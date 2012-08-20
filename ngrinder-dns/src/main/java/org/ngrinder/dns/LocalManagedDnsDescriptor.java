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

/**
 * <p>
 * Constructs the LocalManagedDns and returns references to it.
 * </p>
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class LocalManagedDnsDescriptor implements sun.net.spi.nameservice.NameServiceDescriptor {

	public static final String DNS_PROVIDER_NAME = "LocalManagedDns";

	private static sun.net.spi.nameservice.NameService nameService = null;

	static {
		nameService = new LocalManagedDns();
	}

	/**
	 * @return The string "dns"
	 */
	public String getType() {
		return "dns";
	}

	/**
	 * @return The string "dnsjava"
	 */
	public String getProviderName() {
		return DNS_PROVIDER_NAME;
	}

	/**
	 * This doesn't actually create a name service provider, it returns a
	 * reference to the one that was already created as class load time.
	 * 
	 * @return The dnsjava name service provider
	 */

	public sun.net.spi.nameservice.NameService createNameService() {
		return nameService;
	}
}