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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.ngrinder.monitor.share.JVMUtils;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class JavaVirtualMachineInfo {
	private String address;
	private String commandLine;
	private String displayName;
	private int vmid;
	private boolean isAttachSupported;

	public JavaVirtualMachineInfo(int vmid, String commandLine, boolean isAttachSupported, String address) {
		this.vmid = vmid;
		this.commandLine = commandLine;
		this.address = address;
		this.isAttachSupported = isAttachSupported;
		this.displayName = getDisplayName(commandLine);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getVmid() {
		return vmid;
	}

	public void setVmid(int vmid) {
		this.vmid = vmid;
	}

	public boolean isAttachSupported() {
		return isAttachSupported;
	}

	public void setAttachSupported(boolean isAttachSupported) {
		this.isAttachSupported = isAttachSupported;
	}

	public boolean isManageable() {
		return (address != null);
	}

	public String toString() {
		return commandLine;
	}

	private static String getDisplayName(String commandLine) {
		String[] res = commandLine.split(" ", 2);
		if (res[0].endsWith(".jar")) {
			File jarfile = new File(res[0]);
			String displayName = jarfile.getName();
			if (res.length == 2) {
				displayName += " " + res[1];
			}
			return displayName;
		}
		return commandLine;
	}

	public void loadAgent() throws IOException {
		if (address != null) {
			return;
		}

		if (!isAttachSupported()) {
			throw new IOException("This virtual machine \"" + vmid + "\" does not support dynamic attach.");
		}

		VirtualMachine vm = null;
		String name = String.valueOf(vmid);
		try {
			vm = VirtualMachine.attach(name);
		} catch (AttachNotSupportedException x) {
			IOException ioe = new IOException(x.getMessage(), x);
			ioe.initCause(x);
			throw ioe;
		}

		String home = vm.getSystemProperties().getProperty("java.home");

		String agent = home + File.separator + "jre" + File.separator + "lib" + File.separator + "management-agent.jar";
		File f = new File(agent);
		if (!f.exists()) {
			agent = home + File.separator + "lib" + File.separator + "management-agent.jar";
			f = new File(agent);
			if (!f.exists()) {
				throw new IOException("Management agent not found");
			}
		}

		agent = f.getCanonicalPath();
		try {
			vm.loadAgent(agent, "com.sun.management.jmxremote");
		} catch (AgentLoadException x) {
			IOException ioe = new IOException(x.getMessage(), x);
			ioe.initCause(x);
			throw ioe;
		} catch (AgentInitializationException x) {
			IOException ioe = new IOException(x.getMessage(), x);
			ioe.initCause(x);
			throw ioe;
		}

		Properties agentProps = vm.getAgentProperties();
		address = (String) agentProps.get(JVMUtils.LOCAL_CONNECTOR_ADDRESS);

		vm.detach();

		if (address == null) {
			throw new IOException("Fails to find connector address");
		}
	}

}
