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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ngrinder.monitor.MonitorContext;
import org.ngrinder.monitor.share.domain.JavaVirtualMachineInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;
import sun.management.ConnectorAddressLink;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JVMUtils {

	private static final Logger LOG = LoggerFactory.getLogger(JVMUtils.class);

	public static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	@SuppressWarnings("rawtypes")
	public static Map<Integer, JavaVirtualMachineInfo> getAllJVMs() {
		Map<Integer, JavaVirtualMachineInfo> map = new HashMap<Integer, JavaVirtualMachineInfo>();

		// Monitored VMs
		MonitoredHost host;
		Set vms;
		String name;
		boolean attachable;
		String address;

		try {
			host = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
			vms = host.activeVms();
		} catch (java.net.URISyntaxException sx) {
			throw new InternalError(sx.getMessage());
		} catch (MonitorException mx) {
			throw new InternalError(mx.getMessage());
		}
		for (Object vmidObject : vms) {
			if (vmidObject instanceof Integer) {
				int vmid = ((Integer) vmidObject).intValue();
				if (!MonitorContext.getInstance().getJvmPids().isEmpty()
						&& !MonitorContext.getInstance().getJvmPids().contains(vmid)) {
					continue;
				}
				name = vmidObject.toString();
				attachable = false;
				address = null;

				try {
					MonitoredVm mvm = host.getMonitoredVm(new VmIdentifier(name));

					name = MonitoredVmUtil.commandLine(mvm);
					attachable = MonitoredVmUtil.isAttachable(mvm);
					address = ConnectorAddressLink.importFrom(vmid);
					mvm.detach();
				} catch (Exception x) {
					LOG.error(x.getMessage(), x);
				}
				map.put((Integer) vmidObject, new JavaVirtualMachineInfo(vmid, name, attachable, address));
			}
		}

		// Attachable VMs
		List<VirtualMachineDescriptor> virtualMachines = VirtualMachine.list();
		for (VirtualMachineDescriptor vmd : virtualMachines) {
			try {
				Integer vmid = Integer.valueOf(vmd.id());
				if (!MonitorContext.getInstance().getJvmPids().isEmpty()
						&& !MonitorContext.getInstance().getJvmPids().contains(vmid)) {
					continue;
				}
				if (!map.containsKey(vmid)) {
					attachable = false;
					address = null;

					try {
						VirtualMachine vm = VirtualMachine.attach(vmd);
						attachable = true;
						address = (String) vm.getAgentProperties().get(LOCAL_CONNECTOR_ADDRESS);
						vm.detach();
					} catch (AttachNotSupportedException x) {
						LOG.error(x.getMessage(), x);
					} catch (IOException x) {
						LOG.error(x.getMessage(), x);
					}
					map.put(vmid, new JavaVirtualMachineInfo(vmid.intValue(), vmd.displayName(), attachable, address));
				}
			} catch (NumberFormatException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return map;
	}

	public static int getCurrentJVMPid() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName(); // format: "pid@hostname"
		try {
			return Integer.parseInt(name.substring(0, name.indexOf('@')));
		} catch (Exception e) {
			return -1;
		}
	}
}
