package com.nhncorp.ngrinder.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * nGrinderHanitorAgent
 * 
 * @author He Tangjun
 */
public class NGrinderHanitorAgent implements ServletContextListener {
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(NGrinderHanitorAgent.class);

	private static final String HANITOR_PATH = Thread.currentThread().getContextClassLoader().getResource("").getPath()
			.replaceAll("/classes/$", "/nGrinderHanitorAgent/");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent evt) {
		LOG.info("nGrinderHanitorAgent starting...");
		LOG.info("nGrinderHanitorAgent root:{}", HANITOR_PATH);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				String[] cmdarray = { "/bin/bash", "-c", "chmod a+x " + HANITOR_PATH + "*.sh" };

				try {
					Process premise = Runtime.getRuntime().exec(cmdarray);
					handleProcess(premise);
					premise.waitFor();
					// ./nGrinderServer.sh start -port:4444 -collector:java,system
					// -jvmPid:26185
					Process process = Runtime.getRuntime().exec(
							HANITOR_PATH + "nGrinderServer.sh start -jvmPid:" + getCurrentJVMPid());
					handleProcess(process);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		});

		thread.setDaemon(true);
		thread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent evt) {
		try {
			Process process = Runtime.getRuntime().exec(HANITOR_PATH + "nGrinderServer.sh stop");
			handleProcess(process);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void handleProcess(Process process) {
		new Thread(new HandleRunnable(new BufferedReader(new InputStreamReader(process.getInputStream())))).start();
		new Thread(new HandleRunnable(new BufferedReader(new InputStreamReader(process.getErrorStream())))).start();
	}

	private static class HandleRunnable implements Runnable {
		private final BufferedReader reader;

		public HandleRunnable(BufferedReader reader) {
			this.reader = reader;
		}

		@Override
		public void run() {
			String line;

			try {
				while ((line = reader.readLine()) != null) {
					LOG.info(line);
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

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
