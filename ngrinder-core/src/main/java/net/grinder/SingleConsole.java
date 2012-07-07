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
package net.grinder;

import java.util.ArrayList;
import java.util.List;

import net.grinder.common.GrinderException;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.ConsoleFoundationEx;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.AllocateLowestNumber;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.ReflectionUtil;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single console for multiple test
 * 
 * @author JunHo Yoon
 */
public class SingleConsole {
	private final ConsoleProperties consoleProperties;
	private Thread thread;
	private ConsoleFoundationEx consoleFoundation;
	public final static Resources resources = new ResourcesImplementation(
			"net.grinder.console.common.resources.Console");

	public final static Logger logger = LoggerFactory.getLogger(resources.getString("shortTitle"));

	public Condition m_eventSyncCondition = new Condition();

	public SingleConsole(String ip, int port) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties());
	}

	public SingleConsole(String ip, int port, ConsoleProperties consoleProperties) {
		this.consoleProperties = consoleProperties;

		try {
			this.getConsoleProperties().setConsoleHost(ip);
			this.getConsoleProperties().setConsolePort(port);
			this.consoleFoundation = new ConsoleFoundationEx(resources, logger, consoleProperties, m_eventSyncCondition);
		} catch (GrinderException e) {
			throw new NGrinderRuntimeException("Exception occurs while creating SingleConsole", e);

		}
	}

	public SingleConsole(int port) {
		this("", port);
	}

	public void start() {
		synchronized (m_eventSyncCondition) {
			thread = new Thread(new Runnable() {
				public void run() {
					consoleFoundation.run();
				}
			});
			thread.setDaemon(true);
			thread.start();
			m_eventSyncCondition.waitNoInterrruptException(10000);
		}
	}

	/**
	 * For test
	 */
	public void startSync() {
		consoleFoundation.run();
	}

	public void shutdown() {
		try {
			consoleFoundation.shutdown();
			thread.join();
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Exception occurs while shutting down SingleConsole", e);
		}
	}

	public int getAllAttachedAgentsCount() {
		return ((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class))
				.getNumberOfLiveAgents();
	}

	public List<AgentIdentity> getAllAttachedAgents() {
		final List<AgentIdentity> agentIdentities = new ArrayList<AgentIdentity>();
		AllocateLowestNumber agentIdentity = (AllocateLowestNumber) ReflectionUtil
				.getFieldValue((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class),
						"m_agentNumberMap");
		System.out.println(agentIdentity);
		agentIdentity.forEach(new AllocateLowestNumber.IteratorCallback() {
			public void objectAndNumber(Object object, int number) {
				agentIdentities.add((AgentIdentity) object);
			}
		});
		return agentIdentities;
	}

	/**
	 * @return the consoleFoundation
	 */
	public <T> T getConsoleComponent(Class<T> componentType) {
		return consoleFoundation.getComponent(componentType);
	}

	public ConsoleProperties getConsoleProperties() {
		return consoleProperties;
	}
}
