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
package net.grinder.engine.agent;

import net.grinder.common.UncheckedInterruptedException;
import net.grinder.engine.common.EngineException;
import net.grinder.util.thread.Condition;
import net.grinder.util.thread.ExecutorFactory;
import net.grinder.util.thread.InterruptibleRunnable;
import net.grinder.util.thread.InterruptibleRunnableAdapter;
import org.slf4j.Logger;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * WorkerLauncher which redirect stdout/stderr stream to user.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class ErrorStreamRedirectWorkerLauncher {

	private final ExecutorService m_executor;
	private final WorkerFactory m_workerFactory;
	private final Condition m_notifyOnFinish;
	private final Logger m_logger;

	/**
	 * Fixed size array with a slot for all potential workers. Synchronise on m_workers before
	 * accessing entries. If an entry is null and its index is less than m_nextWorkerIndex, the
	 * worker has finished or the WorkerLauncher has been shutdown.
	 */
	private final Worker[] m_workers;

	/**
	 * The next worker to start. Only increases.
	 */
	private int m_nextWorkerIndex = 0;
	private OutputStream errStream;

	/**
	 * Constructor.
	 *
	 * @param numberOfWorkers worker count
	 * @param workerFactory   worker factory
	 * @param notifyOnFinish  synchronization condition
	 * @param logger          logger
	 * @param errStream       redirect stream
	 */
	public ErrorStreamRedirectWorkerLauncher(int numberOfWorkers, WorkerFactory workerFactory,
	                                         Condition notifyOnFinish, Logger logger, OutputStream errStream) {

		this(ExecutorFactory.createThreadPool("WorkerLauncher", 1), numberOfWorkers, workerFactory, notifyOnFinish,
				logger);
		this.errStream = errStream;
	}

	/**
	 * Package scope for unit tests.
	 *
	 * @param executor        executors
	 * @param numberOfWorkers worker count
	 * @param workerFactory   worker factory
	 * @param notifyOnFinish  synchronization condition
	 * @param logger          logger
	 */
	ErrorStreamRedirectWorkerLauncher(ExecutorService executor, int numberOfWorkers, WorkerFactory workerFactory,
	                                  Condition notifyOnFinish, Logger logger) {
		m_executor = executor;
		m_workerFactory = workerFactory;
		m_notifyOnFinish = notifyOnFinish;
		m_logger = logger;

		m_workers = new Worker[numberOfWorkers];
	}

	/**
	 * Start all workers.
	 *
	 * @throws EngineException engine exception
	 */
	public void startAllWorkers() throws EngineException {
		startSomeWorkers(m_workers.length - m_nextWorkerIndex);
	}

	/**
	 * Start some of all workers.
	 *
	 * @param numberOfWorkers worker count
	 * @return true if all workers is not available.
	 * @throws EngineException engine exception
	 */
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

	/**
	 * Check if all workers are finished.
	 *
	 * @return true if all finished
	 */
	public boolean allFinished() {
		if (m_nextWorkerIndex < m_workers.length) {
			return false;
		}

		synchronized (m_workers) {
			for (Worker m_worker : m_workers) {
				if (m_worker != null) {
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

	/**
	 * Block to start workers anymore.
	 */
	public void dontStartAnyMore() {
		m_nextWorkerIndex = m_workers.length;
	}

	/**
	 * Destroy all workers.
	 */
	public void destroyAllWorkers() {
		dontStartAnyMore();

		synchronized (m_workers) {
			for (Worker m_worker : m_workers) {
				if (m_worker != null) {
					m_worker.destroy();
				}
			}
		}
	}
}
