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
package org.ngrinder.perftest.service;

import static org.ngrinder.common.constant.NGrinderConstants.NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import net.grinder.SingleConsole;
import net.grinder.console.model.ConsoleProperties;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Console manager class which is responsible to console instance management.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class ConsoleManager {
	private static final Logger LOG = LoggerFactory.getLogger(ConsoleManager.class);
	volatile private ArrayBlockingQueue<ConsoleEntry> consoleQueue;
	volatile private List<SingleConsole> consoleInUse = Collections.synchronizedList(new ArrayList<SingleConsole>());

	@Autowired
	private Config config;

	/**
	 * Prepare console queue.
	 */
	@PostConstruct
	public void init() {
		int consoleSize = getConsoleSize();
		consoleQueue = new ArrayBlockingQueue<ConsoleEntry>(consoleSize);
		for (int each : getAvailablePorts(consoleSize, getConsolePortBase())) {
			consoleQueue.add(new ConsoleEntry(each));
		}
	}

	protected int getConsolePortBase() {
		return config.getSystemProperties().getPropertyInt(NGrinderConstants.NGRINDER_PROP_CONSOLE_PORT_BASE,
				NGrinderConstants.NGRINDER_PROP_CONSOLE_PORT_BASE_VALUE);
	}

	protected int getConsoleSize() {
		return config.getSystemProperties().getPropertyInt(NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST,
				NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST_VALUE);
	}

	/**
	 * Timeout (in second).
	 * 
	 * @return 5000 second
	 */
	protected long getMaxWaitingMiliSecond() {
		return config.getSystemProperties().getPropertyInt(NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS,
				NGrinderConstants.NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS_VALUE);
	}

	/**
	 * Get available ports.
	 * 
	 * @param size
	 *            port size
	 * @param from
	 *            port number starting from
	 * @return port list
	 */
	List<Integer> getAvailablePorts(int size, int from) {
		List<Integer> ports = new ArrayList<Integer>();
		int freeSocket;
		for (int i = 0; i < size; i++) {
			freeSocket = checkPortAvailability(from);
			ports.add(freeSocket);
			from = freeSocket + 1;
		}
		return ports;
	}

	/**
	 * Get a available port greater than the given port.
	 * 
	 * @param scanStartPort
	 *            port scan from
	 * @return min port available from scanStartPort
	 */
	private int checkPortAvailability(int scanStartPort) {
		while (true) {
			if (checkExactPortAvailability(scanStartPort)) {
				return scanStartPort;
			}
			if (scanStartPort++ > 65000) {
				throw new NGrinderRuntimeException("no port for console is available");
			}
		}
	}

	/**
	 * Check the given port is empty
	 * 
	 * @param port
	 *            port to be checked
	 * @return port
	 */
	private boolean checkExactPortAvailability(int port) {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Get available console.
	 * 
	 * If there is no available console, it waits until available console is returned back. If the specific time is
	 * elapsed, the timeout error occurs and throw {@link NGrinderRuntimeException}. timeout can be adjusted by
	 * overriding {@link #getMaxWaitingMiliSecond()}.
	 * 
	 * @param baseConsoleProperties
	 *            base {@link ConsoleProperties}
	 * @return console
	 */
	public SingleConsole getAvailableConsole(ConsoleProperties baseConsoleProperties) {
		ConsoleEntry consoleEntry;
		try {
			consoleEntry = consoleQueue.poll(getMaxWaitingMiliSecond(), TimeUnit.MILLISECONDS);
			if (consoleEntry == null) {
				throw new NGrinderRuntimeException("no console entry available");
			}
			synchronized (this) {
				// FIXME : It might fail here
				SingleConsole singleConsole = new SingleConsole(consoleEntry.getPort(), baseConsoleProperties);
				getConsoleInUse().add(singleConsole);
				return singleConsole;
			}
		} catch (InterruptedException e) {
			throw new NGrinderRuntimeException("no console entry available");
		}
	}

	/**
	 * Return back console.
	 * 
	 * Duplicated returns is allowed.
	 * 
	 * @param console
	 *            console which will be returned back.
	 * 
	 */
	public void returnBackConsole(SingleConsole console) {
		synchronized (this) {
			try {
				console.unregisterSampling();
				console.sendStopMessageToAgents();
			} catch (Exception e) {
				LOG.error("Exception occurs while shuttdowning console in returnback process", e);
				// But the port is getting back.
				// FIXME : Is it OK?
			} finally {
				try {
					Thread.sleep(1000);
					console.shutdown();
				} catch (Exception e) {
					LOG.error("Exception occurs while shuttdowning console in returnback process", e);
					// But the port is getting back.
					// FIXME : Is it OK?
				}
			}
			ConsoleEntry consoleEntry = new ConsoleEntry(console.getConsolePort());

			if (!consoleQueue.contains(consoleEntry)) {
				consoleQueue.add(consoleEntry);
				if (!getConsoleInUse().contains(console)) {
					LOG.error("Try to return back the not used console on {} port", console.getConsolePort());
				}
				getConsoleInUse().remove(console);
			}
		}
	}

	/**
	 * Get list of {@link SingleConsole} which are used.
	 * 
	 * @return {@link SingleConsole} list in use
	 */
	public List<SingleConsole> getConsoleInUse() {
		return consoleInUse;
	}

	/**
	 * Get the size of available consoles.
	 * 
	 * @return size of available consoles.
	 */
	public Integer getAvailableConsoleSize() {
		return consoleQueue.size();
	}

	/**
	 * Get {@link SingleConsole} instance which uses the given port.
	 * 
	 * @param port
	 *            port which will be checked against
	 * @return {@link SingleConsole} instance if found. Otherwise, null
	 */
	public SingleConsole getConsoleUsingPort(int port) {
		for (SingleConsole each : consoleInUse) {
			if (each.getConsolePort() == port) {
				return each;
			}
		}
		return null;
	}

}
