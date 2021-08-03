// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000 - 2011 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.engine.process;

import net.grinder.common.GrinderProperties;
import net.grinder.engine.common.EngineException;
import net.grinder.scriptengine.ScriptEngineService.WorkerRunnable;
import net.grinder.scriptengine.ScriptExecutionException;
import net.grinder.util.Sleeper;

import org.slf4j.Logger;


/**
 * The class executed by each thread.
 *
 * @author Paco Gomez
 * @author Philip Aston
 */
class GrinderThread implements Runnable {

	private final Logger m_logger;
	private final WorkerThreadSynchronisation m_threadSynchronisation;
	private final ProcessLifeCycleListener m_processLifeCycle;
	private final GrinderProperties m_properties;
	private final Sleeper m_sleeper;
	private final ThreadContext m_context;
	private final WorkerRunnableFactory m_workerRunnableFactory;

	/**
	 * The constructor.
	 */
	public GrinderThread(Logger logger,
						 ThreadContext context,
						 WorkerThreadSynchronisation threadSynchronisation,
						 ProcessLifeCycleListener processLifeCycle,
						 GrinderProperties properties,
						 Sleeper sleeper,
						 WorkerRunnableFactory workerRunnableFactory)
		throws EngineException {

		m_logger = logger;
		m_context = context;
		m_threadSynchronisation = threadSynchronisation;
		m_processLifeCycle = processLifeCycle;
		m_properties = properties;
		m_sleeper = sleeper;
		m_workerRunnableFactory = workerRunnableFactory;

		// Dispatch the process context callback in the main thread.
		m_processLifeCycle.threadCreated(m_context);

		m_threadSynchronisation.threadCreated();
	}

	/**
	 * The thread's main loop.
	 */
	public void run() {
		m_processLifeCycle.threadStarted(m_context);

		m_context.setCurrentRunNumber(-1);

		// Fire begin thread event before creating the worker runnable to allow
		// plug-ins to do per-thread initialisation required by the script code.
		m_context.fireBeginThreadEvent();

		try {
			final WorkerRunnable workerRunnable = m_workerRunnableFactory.create();

			final int numberOfRuns = m_properties.getInt("grinder.runs", 1);

			if (numberOfRuns == 0) {
				m_logger.info(m_context.getLogMarker(), "starting, will run forever");
			}
			else {
				m_logger.info(m_context.getLogMarker(),
					"starting, will do {} run{}",
					numberOfRuns,
					numberOfRuns == 1 ? "" : "s");
			}

			m_threadSynchronisation.awaitStart();

			m_sleeper.sleepFlat(m_properties.getLong("grinder.initialSleepTime", 0));

			int currentRun;

			for (currentRun = 0;
				 numberOfRuns == 0 || currentRun < numberOfRuns;
				 currentRun++) {

				m_context.setCurrentRunNumber(currentRun);

				m_context.fireBeginRunEvent();

				try {
					workerRunnable.run();
				}
				// [#817](https://github.com/naver/ngrinder/pull/817) Add for nGrinder
				catch (ShutdownException e) {
					m_logger.info(m_context.getLogMarker(), "Thread has been shut down");
					break;
				}
				// [#817] end
				catch (ScriptExecutionException e) {
					final Throwable cause = e.getCause();

					if (cause instanceof ShutdownException ||
						cause instanceof Sleeper.ShutdownException) {
						m_logger.info(m_context.getLogMarker(), "shut down");
						break;
					}

					m_logger.error(m_context.getLogMarker(),
						"Aborted run: " + e.getShortMessage(),
						e);
				}

				m_context.fireEndRunEvent();
			}

			m_context.setCurrentRunNumber(-1);

			m_logger.info(m_context.getLogMarker(),
				"finished {} {}",
				currentRun,
				currentRun == 1 ? "run" : "runs");

			m_context.fireBeginShutdownEvent();

			try {
				workerRunnable.shutdown();
			}
			catch (ScriptExecutionException e) {
				m_logger.error(m_context.getLogMarker(),
					"Aborted test runner shut down: " + e.getShortMessage(),
					e);
			}

			m_context.fireEndThreadEvent();
		}
		catch (ScriptExecutionException e) {
			m_logger.error(m_context.getLogMarker(),
				"Aborting thread: {}" + e.getShortMessage(),
				e);
		}
		catch (Exception e) {
			m_logger.error(m_context.getLogMarker(), "Aborting thread: " + e, e);
		}
		finally {
			m_context.setCurrentRunNumber(-1);

			m_threadSynchronisation.threadFinished();
		}
	}
}
