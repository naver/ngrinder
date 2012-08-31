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
package org.ngrinder.sm;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * nGrinder security manager
 * 
 * @author JunHo Yoon
 * @author Tobi
 * @since 3.0
 */
public class NGrinderSecurityManager extends SecurityManager {

	private String workDirectory = System.getProperty("user.dir");
	private String agentExecDirectory = System.getProperty("ngrinder.exec.path", workDirectory);
	private String etcHosts = System.getProperty("ngridner.etc.hosts", "");
	private List<String> hostIPList = this.getHostIP(etcHosts);

	@Override
	public void checkPropertiesAccess() {
		// allow all properties access
	}

	@Override
	public void checkPropertyAccess(String key) {
		// allow all properties access
	}

	@Override
	public void checkRead(String file) {
		if (!(fileAccessAllowedInWorkDirectory(file) || fileAccessAllowedInAgentExecDirectory(file))) {
			throw new SecurityException("File read access on " + file + " is not allowed.");
		}
	}

	@Override
	public void checkRead(String file, Object context) {
		if (!(fileAccessAllowedInWorkDirectory(file) || fileAccessAllowedInAgentExecDirectory(file))) {
			throw new SecurityException("File read access on " + file + " is not allowed.");
		}
	}

	@Override
	public void checkWrite(String file) {
		if (!(fileAccessAllowedInWorkDirectory(file))) {
			throw new SecurityException("File write read access on " + file + " is not allowed.");
		}
	}

	@Override
	public void checkDelete(String file) {
		if (!(fileAccessAllowedInWorkDirectory(file))) {
			throw new SecurityException("File delete read access on " + file + " is not allowed.");
		}
	}

	@Override
	public void checkExec(String cmd) {
		throw new SecurityException("Cmd execution of " + cmd + " is not allowed.");
	}

	/**
	 * File access is allowed on "user.dir"
	 * 
	 * @param file
	 */
	private boolean fileAccessAllowedInWorkDirectory(String file) {
		return new File(file).getAbsolutePath().startsWith(workDirectory);
	}

	/**
	 * File access is allowed on "agent.exec.folder"
	 * 
	 * @param file
	 */
	private boolean fileAccessAllowedInAgentExecDirectory(String file) {
		return new File(file).getAbsolutePath().startsWith(agentExecDirectory);
	}

	@Override
	public void checkExit(int status) {
		// Always block
		throw new SecurityException("System.exit execution of  is not allowed.");
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		throw new SecurityException("Multicast on " + maddr.toString() + " is not always allowed.");
	}

	@Override
	public void checkConnect(String host, int port) {
		this.netWorkAccessAllowed(host);
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		this.netWorkAccessAllowed(host);
	}

	/**
	 * NetWork access is allowed on "ngridner.etc.hosts"
	 * 
	 * @param host
	 */
	private void netWorkAccessAllowed(String host) {
		if (hostIPList.contains(host)) {
			return;
		}
		throw new SecurityException("NetWork access on " + host + " is not allowed.");
	}

	/**
	 * Get ip address of target hosts. <br>
	 * if target hosts 'a.com:1.1.1.1' add ip: '1.1.1.1' <br>
	 * if target hosts ':1.1.1.1' add ip: '1.1.1.1' <br>
	 * if target hosts '1.1.1.1' add ip: '1.1.1.1' <br>
	 * 
	 * @return
	 */
	private List<String> getHostIP(String hostString) {
		List<String> ipList = new ArrayList<String>();
		String[] hostsList = StringUtils.split(hostString, ",");
		for (String hosts : hostsList) {
			String[] addresses = StringUtils.split(hosts, ":");
			if (addresses.length > 0) {
				ipList.add(addresses[addresses.length - 1]);
			} else {
				ipList.add(hosts);
			}
		}
		return ipList;
	}
}
