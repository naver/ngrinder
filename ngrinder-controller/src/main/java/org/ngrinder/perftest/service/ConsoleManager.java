/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.perftest.service;

import net.grinder.SingleConsole;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.console.model.ConsoleProperties;
import org.h2.util.StringUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.perftest.model.NullSingleConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static net.grinder.util.NetworkUtils.getAvailablePorts;
import static org.ngrinder.common.constant.ControllerConstants.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Console manager is responsible for console instance management.
 * <p/>
 * A number of consoles(specified in ngrinder.maxConcurrentTest in system.conf) are pooled. Actually console itself is
 * not pooled but the {@link ConsoleEntry} which contains console information are pooled internally. Whenever a user
 * requires a new console, it gets the one {@link ConsoleEntry} from the pool and creates new console with the
 * {@link ConsoleEntry}. Currently using consoles are kept in {@link #consoleInUse} member variable.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class ConsoleManager {
	private static final int MAX_PORT_NUMBER = 65000;
	private static final Logger LOG = LoggerFactory.getLogger(ConsoleManager.class);
	private volatile ArrayBlockingQueue<ConsoleEntry> consoleQueue;
	private volatile List<SingleConsole> consoleInUse = Collections.synchronizedList(new ArrayList<SingleConsole>());

	@Autowired
	private Config config;

	@Autowired
	private AgentManager agentManager;


	/**
	 * Prepare console queue.
	 */
	@PostConstruct
	public void init() {
		int consoleSize = getConsoleSize();
		consoleQueue = new ArrayBlockingQueue<ConsoleEntry>(consoleSize);
		final String currentIP = config.getCurrentIP();
		for (int each : getAvailablePorts(currentIP, consoleSize, getConsolePortBase(), MAX_PORT_NUMBER)) {
			final ConsoleEntry e = new ConsoleEntry(config.getCurrentIP(), each);
			try {
				e.occupySocket();
				consoleQueue.add(e);
			} catch (Exception ex) {
				LOG.error("socket binding to {}:{} is failed", config.getCurrentIP(), each);
			}

		}
	}

	/**
	 * Get the base port number of console.
	 * <p/>
	 * It can be specified at ngrinder.consolePortBase in system.conf. Each console will be created from that port.
	 *
	 * @return base port number
	 */
	protected int getConsolePortBase() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_CONSOLE_PORT_BASE);
	}

	/**
	 * Get the console pool size. It can be specified at ngrinder.maxConcurrentTest in system.conf.
	 *
	 * @return console size.
	 */
	protected int getConsoleSize() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_CONCURRENT_TEST);
	}

	/**
	 * Get Timeout (in second).
	 *
	 * @return 5000 second
	 */
	protected long getMaxWaitingMilliSecond() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_CONNECTION_WAITING_MILLISECOND);
	}


	/**
	 * Get an available console.
	 * <p/>
	 * If there is no available console, it waits until available console is returned back. If the specific time is
	 * elapsed, the timeout error occurs and throws {@link org.ngrinder.common.exception.NGrinderRuntimeException} . The
	 * timeout can be adjusted by overriding {@link #getMaxWaitingMilliSecond()}.
	 *
	 * @param baseConsoleProperties base {@link net.grinder.console.model.ConsoleProperties}
	 * @return console
	 */
	public SingleConsole getAvailableConsole(ConsoleProperties baseConsoleProperties) {
		ConsoleEntry consoleEntry = null;
		try {
			consoleEntry = consoleQueue.poll(getMaxWaitingMilliSecond(), TimeUnit.MILLISECONDS);
			if (consoleEntry == null) {
				throw processException("no console entry available");
			}
			synchronized (this) {
				consoleEntry.releaseSocket();
				// FIXME : It might fail here
				ConsoleCommunicationSetting consoleCommunicationSetting = ConsoleCommunicationSetting.asDefault();
				if (config.getInactiveClientTimeOut() > 0) {
					consoleCommunicationSetting.setInactiveClientTimeOut(config.getInactiveClientTimeOut());
				}
				SingleConsole singleConsole = new SingleConsole(config.getCurrentIP(), consoleEntry.getPort(),
						consoleCommunicationSetting, baseConsoleProperties);
				getConsoleInUse().add(singleConsole);
				singleConsole.setCsvSeparator(config.getCsvSeparator());
				return singleConsole;
			}
		} catch (Exception e) {
			if (consoleEntry != null) {
				consoleQueue.add(consoleEntry);
			}
			throw processException("no console entry available");
		}
	}

	/**
	 * Return back the given console.
	 * <p/>
	 * Duplicated returns is allowed.
	 *
	 * @param testIdentifier test identifier
	 * @param console        console which will be returned back.
	 */
	public void returnBackConsole(String testIdentifier, SingleConsole console) {
		if (console == null || console instanceof NullSingleConsole) {
			LOG.error("Attempt to return back null console for {}.", testIdentifier);
			return;
		}
		try {
			console.sendStopMessageToAgents();
		} catch (Exception e) {
			LOG.error("Exception occurred during console return back for test {}.",
					testIdentifier, e);
			// But the port is getting back.
		} finally {
			// This is very careful implementation..
			try {
				// Wait console is completely shutdown...
				console.waitUntilAllAgentDisconnected();
			} catch (Exception e) {
				LOG.error("Exception occurred during console return back for test {}.",
						testIdentifier, e);
				// If it's not disconnected still, stop them by force.
				agentManager.stopAgent(console.getConsolePort());
			}
			try {
				console.shutdown();
			} catch (Exception e) {
				LOG.error("Exception occurred during console return back for test {}.",
						testIdentifier, e);
			}
			int consolePort;
			String consoleIP;
			try {
				consolePort = console.getConsolePort();
				consoleIP = console.getConsoleIP();
				ConsoleEntry consoleEntry = new ConsoleEntry(consoleIP, consolePort);
				synchronized (this) {
					if (!consoleQueue.contains(consoleEntry)) {
						consoleEntry.occupySocket();
						consoleQueue.add(consoleEntry);
						if (!getConsoleInUse().contains(console)) {
							LOG.error("Try to return back the not used console on {} port", consolePort);
						}
						getConsoleInUse().remove(console);
					}
				}
			} catch (Exception e) {
				noOp();
			}
		}
	}

	/**
	 * Get the list of {@link SingleConsole} which are used.
	 *
	 * @return {@link SingleConsole} list in use
	 */
	public List<SingleConsole> getConsoleInUse() {
		return consoleInUse;
	}

	/**
	 * Get the size of currently available consoles.
	 *
	 * @return size of available consoles.
	 */
	public Integer getAvailableConsoleSize() {
		return consoleQueue.size();
	}

	/**
	 * Get the {@link SingleConsole} instance which is using the given port.
	 *
	 * @param port port which the console is using
	 * @return {@link SingleConsole} instance if found. Otherwise, {@link NullSingleConsole} instance.
	 */
	public SingleConsole getConsoleUsingPort(Integer port) {
		String currentIP = config.getCurrentIP();
		for (SingleConsole each : consoleInUse) {
			// Avoid to Klocwork error.
			if (each instanceof NullSingleConsole) {
				continue;
			}
			if (StringUtils.equals(each.getConsoleIP(), currentIP) && each.getConsolePort() == port) {
				return each;
			}
		}
		return new NullSingleConsole();
	}

}
