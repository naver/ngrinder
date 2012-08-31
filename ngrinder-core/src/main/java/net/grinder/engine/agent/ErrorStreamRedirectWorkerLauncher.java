package net.grinder.engine.agent;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import net.grinder.common.UncheckedInterruptedException;
import net.grinder.engine.common.EngineException;
import net.grinder.util.thread.Condition;
import net.grinder.util.thread.ExecutorFactory;
import net.grinder.util.thread.InterruptibleRunnable;
import net.grinder.util.thread.InterruptibleRunnableAdapter;

import org.slf4j.Logger;

public class ErrorStreamRedirectWorkerLauncher {

	private final ExecutorService m_executor;
	private final WorkerFactory m_workerFactory;
	private final Condition m_notifyOnFinish;
	private final Logger m_logger;

	/**
	 * Fixed size array with a slot for all potential workers. Synchronise on
	 * m_workers before accessing entries. If an entry is null and its index is
	 * less than m_nextWorkerIndex, the worker has finished or the
	 * WorkerLauncher has been shutdown.
	 */
	private final Worker[] m_workers;

	/**
	 * The next worker to start. Only increases.
	 */
	private int m_nextWorkerIndex = 0;
	private OutputStream errStream;

	public ErrorStreamRedirectWorkerLauncher(int numberOfWorkers, WorkerFactory workerFactory,
			Condition notifyOnFinish, Logger logger, OutputStream errStream) {

		this(ExecutorFactory.createThreadPool("WorkerLauncher", 1), numberOfWorkers, workerFactory, notifyOnFinish,
				logger);
		this.errStream = errStream;
	}

	/**
	 * Package scope for unit tests.
	 */
	ErrorStreamRedirectWorkerLauncher(ExecutorService executor, int numberOfWorkers, WorkerFactory workerFactory,
			Condition notifyOnFinish, Logger logger) {
		m_executor = executor;
		m_workerFactory = workerFactory;
		m_notifyOnFinish = notifyOnFinish;
		m_logger = logger;

		m_workers = new Worker[numberOfWorkers];
	}

	public void startAllWorkers() throws EngineException {
		startSomeWorkers(m_workers.length - m_nextWorkerIndex);
	}

	public boolean startSomeWorkers(int numberOfWorkers) throws EngineException {

		final int numberToStart = Math.min(numberOfWorkers, m_workers.length - m_nextWorkerIndex);

		for (int i = 0; i < numberToStart; ++i) {
			final int workerIndex = m_nextWorkerIndex;

			final Worker worker = m_workerFactory.create(errStream, errStream);

			synchronized (m_workers) {
				m_workers[workerIndex] = worker;
			}

			try {
				m_executor.execute(new InterruptibleRunnableAdapter(new WaitForWorkerTask(workerIndex)));
			} catch (RejectedExecutionException e) {
				m_logger.error("Failed to wait for " + worker.getIdentity().getName(), e);
				worker.destroy();
				return false;
			}

			m_logger.info("worker " + worker.getIdentity().getName() + " started");

			++m_nextWorkerIndex;
		}

		return m_workers.length > m_nextWorkerIndex;
	}

	private final class WaitForWorkerTask implements InterruptibleRunnable {

		private final int m_workerIndex;

		public WaitForWorkerTask(int workerIndex) {
			m_workerIndex = workerIndex;
		}

		public void interruptibleRun() {
			final Worker worker;

			synchronized (m_workers) {
				worker = m_workers[m_workerIndex];
			}

			assert worker != null;

			try {
				worker.waitFor();
			} catch (UncheckedInterruptedException e) {
				// We're taking our worker down with us.
				worker.destroy();
			}

			synchronized (m_workers) {
				m_workers[m_workerIndex] = null;
			}

			if (allFinished()) {
				synchronized (m_notifyOnFinish) {
					m_notifyOnFinish.notifyAll();
				}
			}
		}
	}

	public boolean allFinished() {
		if (m_nextWorkerIndex < m_workers.length) {
			return false;
		}

		synchronized (m_workers) {
			for (int i = 0; i < m_workers.length; i++) {
				if (m_workers[i] != null) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Request shutdown of the worker launcher threads. Returns immediately.
	 */
	public void shutdown() {
		m_executor.shutdown();
	}

	public void dontStartAnyMore() {
		m_nextWorkerIndex = m_workers.length;
	}

	public void destroyAllWorkers() {
		dontStartAnyMore();

		synchronized (m_workers) {
			for (int i = 0; i < m_workers.length; i++) {
				if (m_workers[i] != null) {
					m_workers[i].destroy();
				}
			}
		}
	}
}
