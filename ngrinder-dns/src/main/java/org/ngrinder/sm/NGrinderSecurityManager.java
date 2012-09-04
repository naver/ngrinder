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
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

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
	private String javaHomeDirectory = System.getenv("JAVA_HOME");
	private String etcHosts = System.getProperty("ngridner.etc.hosts", "");
	private String consoleIP = System.getProperty("ngrinder.console.ip", "127.0.0.1");
	private List<String> allowedHost = new ArrayList<String>();

	{
		/**
		 * Get ip address of target hosts. <br>
		 * if target hosts 'a.com:1.1.1.1' add ip: '1.1.1.1' <br>
		 * if target hosts ':1.1.1.1' add ip: '1.1.1.1' <br>
		 * if target hosts '1.1.1.1' add ip: '1.1.1.1' <br>
		 */
		String[] hostsList = etcHosts.split(",");
		for (String hosts : hostsList) {
			String[] addresses = hosts.split(":");
			if (addresses.length > 0) {
				allowedHost.add(addresses[0]);
				allowedHost.add(addresses[addresses.length - 1]);
			} else {
				allowedHost.add(hosts);
			}
		}

		/**
		 * add controler host
		 */
		allowedHost.add(consoleIP);
	}

	@Override
	public void checkPermission(Permission permission) {
		if (permission instanceof java.lang.RuntimePermission) {
			// except setSecurityManager
			String permissionName = permission.getName();
			if ("setSecurityManager".equals(permissionName)) {
				throw new SecurityException("java.lang.RuntimePermission: setSecurityManager is not allowed.");
			}
		} else if (permission instanceof java.security.UnresolvedPermission) {
			throw new SecurityException("java.security.UnresolvedPermission is not allowed.");
		} else if (permission instanceof java.awt.AWTPermission) {
			throw new SecurityException("java.awt.AWTPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.AuthPermission) {
			throw new SecurityException("javax.security.auth.AuthPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.PrivateCredentialPermission) {
			throw new SecurityException("javax.security.auth.PrivateCredentialPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.kerberos.DelegationPermission) {
			throw new SecurityException("javax.security.auth.kerberos.DelegationPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.kerberos.ServicePermission) {
			throw new SecurityException("javax.security.auth.kerberos.ServicePermission is not allowed.");
		} else if (permission instanceof javax.sound.sampled.AudioPermission) {
			throw new SecurityException("javax.sound.sampled.AudioPermission is not allowed.");
		}
	}

	@Override
	public void checkPermission(Permission permission, Object context) {
		this.checkPermission(permission);
	}

	@Override
	public void checkRead(String file) {
		this.fileAccessReadAllowed(file);
	}

	@Override
	public void checkRead(String file, Object context) {
		this.fileAccessReadAllowed(file);
	}

	@Override
	public void checkWrite(String file) {
		this.fileAccessWriteAllowed(file);
	}

	@Override
	public void checkDelete(String file) {
		this.fileAccessDeleteAllowed(file);
	}

	@Override
	public void checkExec(String cmd) {
		throw new SecurityException("Cmd execution of " + cmd + " is not allowed.");
	}

	/**
	 * File read access is allowed on <br>
	 * "agent.exec.folder" and "agent.exec.folder"
	 * 
	 * @param file
	 */
	private void fileAccessReadAllowed(String file) {
		String filePath = new File(file).getAbsolutePath();
		if (filePath.startsWith(workDirectory) || filePath.startsWith(agentExecDirectory)
				|| filePath.startsWith(javaHomeDirectory)) {
			return;
		}
		super.checkRead(file);
	}

	/**
	 * File write access is allowed <br>
	 * on "agent.exec.folder"
	 * 
	 * @param file
	 */
	private void fileAccessWriteAllowed(String file) {
		if (new File(file).getAbsolutePath().startsWith(workDirectory)) {
			return;
		}
		super.checkWrite(file);
	}

	/**
	 * File delete access is allowed <br>
	 * on "agent.exec.folder"
	 * 
	 * @param file
	 */
	private void fileAccessDeleteAllowed(String file) {
		if (new File(file).getAbsolutePath().startsWith(workDirectory)) {
			return;
		}
		super.checkDelete(file);
	}

	@Override
	public void checkExit(int status) {
		// Always block
		// throw new
		// SecurityException("System.exit execution of  is not allowed.");
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
		if (allowedHost.contains(host)) {
			return;
		}
		throw new SecurityException("NetWork access on " + host + " is not allowed.");
	}

}
