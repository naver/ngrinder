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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.UncheckedInterruptedException;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.ConsoleFoundationEx;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControl.Listener;
import net.grinder.console.communication.ProcessControl.ProcessReports;
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.distribution.AgentCacheState;
import net.grinder.console.distribution.FileDistribution;
import net.grinder.console.distribution.FileDistributionHandler;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.AllocateLowestNumber;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;
import net.grinder.util.FileContents;
import net.grinder.util.ReflectionUtil;
import net.grinder.util.thread.Condition;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single console for multiple test.
 * 
 * @author JunHo Yoon
 */
public class SingleConsole implements Listener {
	private final ConsoleProperties consoleProperties;
	private Thread thread;
	private ThreadGroup threadGroup;
	private ConsoleFoundationEx consoleFoundation;
	public static final Resources RESOURCE = new ResourcesImplementation("net.grinder.console.common.resources.Console");
	public static final Logger LOGGER = LoggerFactory.getLogger(RESOURCE.getString("shortTitle"));

	private Condition m_eventSyncCondition = new Condition();
	private ProcessReports[] processReports;
	private boolean cancel = false;

	/**
	 * Constructor with console ip and port.
	 * 
	 * @param ip
	 *            IP
	 * @param port
	 *            PORT
	 */
	public SingleConsole(String ip, int port) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties());
	}

	/**
	 * Constructor with console port and properties.
	 * 
	 * @param port
	 *            PORT
	 * @param consoleProperties
	 *            {@link ConsoleProperties} used.
	 */
	public SingleConsole(int port, ConsoleProperties consoleProperties) {
		this("", port, consoleProperties);
	}

	/**
	 * Constructor with IP, port, and properties.
	 * 
	 * @param ip
	 *            IP
	 * @param port
	 *            PORT
	 * @param consoleProperties
	 *            {@link ConsoleProperties} used.
	 */
	public SingleConsole(String ip, int port, ConsoleProperties consoleProperties) {
		this.consoleProperties = consoleProperties;

		try {
			this.getConsoleProperties().setConsoleHost(ip);
			this.getConsoleProperties().setConsolePort(port);
			this.consoleFoundation = new ConsoleFoundationEx(RESOURCE, LOGGER, consoleProperties, m_eventSyncCondition);
		} catch (GrinderException e) {
			throw new NGrinderRuntimeException("Exception occurs while creating SingleConsole", e);

		}
	}

	/**
	 * Simple constructor only setting port. It automatically binds all ip
	 * addresses.
	 * 
	 * @param port
	 *            PORT number
	 */
	public SingleConsole(int port) {
		this("", port);
	}

	/**
	 * Return the assigned console port.
	 * 
	 * @return console port
	 */
	public int getConsolePort() {
		return this.getConsoleProperties().getConsolePort();
	}

	/**
	 * Return the assigned console host. If it's empty, it returns host IP
	 * 
	 * @return console host
	 */
	public String getConsoleHost() {
		try {
			return StringUtils.defaultIfBlank(this.getConsoleProperties().getConsoleHost(), InetAddress.getLocalHost()
					.getHostAddress());
		} catch (UnknownHostException e) {
			return "";
		}
	}

	/**
	 * Start console and wait until it's ready to get agent message.
	 */
	public void start() {

		synchronized (m_eventSyncCondition) {
			this.threadGroup = new ThreadGroup("SingleConsole ThreadGroup on port " + getConsolePort());
			thread = new Thread(threadGroup, new Runnable() {
				public void run() {
					consoleFoundation.run();
				}
			}, "SingleConsole on port " + getConsolePort());
			thread.setDaemon(true);
			thread.start();
			// 10 second is too big?
			m_eventSyncCondition.waitNoInterrruptException(10000);
			getConsoleComponent(ProcessControl.class).addProcessStatusListener(this);
		}
	}

	/**
	 * For test.
	 */
	public void startSync() {
		consoleFoundation.run();
	}

	/**
	 * Shutdown console and wait until underlying console logic is stop to run.
	 */
	public void shutdown() {
		try {
			synchronized (this) {
				consoleFoundation.shutdown();
				if (thread != null) {
					threadGroup.interrupt();
					thread.join();
					thread = null;
					threadGroup = null;
				}
			}
		} catch (InterruptedException e) {
			throw new UncheckedInterruptedException(e);
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Exception occurs while shutting down SingleConsole", e);
		}
	}

	public int getAllAttachedAgentsCount() {
		return ((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class))
				.getNumberOfLiveAgents();
	}

	/**
	 * Get all attached agent list on this console.
	 * 
	 * @return agent list
	 */
	public List<AgentIdentity> getAllAttachedAgents() {
		final List<AgentIdentity> agentIdentities = new ArrayList<AgentIdentity>();
		AllocateLowestNumber agentIdentity = (AllocateLowestNumber) ReflectionUtil
				.getFieldValue((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class),
						"m_agentNumberMap");
		agentIdentity.forEach(new AllocateLowestNumber.IteratorCallback() {
			public void objectAndNumber(Object object, int number) {
				agentIdentities.add((AgentIdentity) object);
			}
		});
		return agentIdentities;
	}

	/**
	 * Get the console Component.
	 * 
	 * @param <T>
	 *            componentType component type
	 * @param componentType
	 *            component type
	 * @return the consoleFoundation
	 */
	public <T> T getConsoleComponent(Class<T> componentType) {
		return consoleFoundation.getComponent(componentType);
	}

	public ConsoleProperties getConsoleProperties() {
		return consoleProperties;
	}

	public void startTest(GrinderProperties properties) {
		properties.setInt(GrinderProperties.CONSOLE_PORT, getConsolePort());
		getConsoleComponent(ProcessControl.class).startWorkerProcesses(properties);
	}

	public void setDistributionDirectory(File filePath) {
		final ConsoleProperties properties = (ConsoleProperties) getConsoleComponent(ConsoleProperties.class);
		Directory directory;
		try {
			directory = new Directory(filePath);
			properties.setAndSaveDistributionDirectory(directory);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new NGrinderRuntimeException(e.getMessage(), e);
		}
	}

	public void cancel() {
		cancel = true;
	}

	private boolean shouldEnable(FileDistribution fileDistribution) {
		return fileDistribution.getAgentCacheState().getOutOfDate();
	}

	/**
	 * Distribute files on given filePath to attached agents.
	 * 
	 * @param filePath
	 *            the distribution files
	 */
	public void distributeFiles(File filePath) {
		setDistributionDirectory(filePath);
		distributFiles();
	}

	/**
	 * Distribute files on agents.
	 */
	public void distributFiles() {
		final FileDistribution fileDistribution = (FileDistribution) getConsoleComponent(FileDistribution.class);
		final AgentCacheState agentCacheState = fileDistribution.getAgentCacheState();
		final Condition cacheStateCondition = new Condition();
		agentCacheState.addListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ignored) {
				synchronized (cacheStateCondition) {
					cacheStateCondition.notifyAll();
				}
			}
		});
		final FileDistributionHandler distributionHandler = fileDistribution.getHandler();
		// When cancel is called.. stop processing.
		while (!cancel) {
			try {
				final FileDistributionHandler.Result result = distributionHandler.sendNextFile();
				if (result == null) {
					break;
				}

			} catch (FileContents.FileContentsException e) {
				// FIXME : Forward this error to controller.!!
				e.printStackTrace();
			}
			// The cache status is updated asynchronously by agent reports.
			// If we have a listener, we wait for up to five seconds for all
			// agents to indicate that they are up to date.
			synchronized (cacheStateCondition) {
				for (int i = 0; i < 5 && shouldEnable(fileDistribution); ++i) {
					cacheStateCondition.waitNoInterrruptException(1000);
				}
			}
		}
	}

	public void waitUntilAgentConnected(int size) {
		int trial = 1;
		while (trial++ < 5) {
			if (this.processReports.length != size) {
				synchronized (m_eventSyncCondition) {
					m_eventSyncCondition.waitNoInterrruptException(1000);
				}
			} else {
				return;
			}
		}
		throw new NGrinderRuntimeException("Connection is not completed "
				+ ToStringBuilder.reflectionToString(processReports));
	}

	@Override
	public void update(ProcessReports[] processReports) {
		synchronized (m_eventSyncCondition) {
			this.processReports = processReports;
			m_eventSyncCondition.notifyAll();
		}
	}

	public boolean isAllTestFinished() {
		for (ProcessReports processReport : this.processReports) {
			// TODO
		}
		return true;
	}

}
