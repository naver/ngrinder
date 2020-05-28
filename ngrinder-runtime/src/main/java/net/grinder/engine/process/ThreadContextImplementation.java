// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000 - 2012 Philip Aston
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

import java.util.ArrayList;
import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.common.SSLContextFactory;
import net.grinder.common.SkeletonThreadLifeCycleListener;
import net.grinder.common.Test;
import net.grinder.common.ThreadLifeCycleListener;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.process.DispatchContext.DispatchStateException;
import net.grinder.script.Statistics.StatisticsForTest;
import net.grinder.statistics.StatisticsServices;
import net.grinder.statistics.StatisticsSet;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Package scope.
 * 
 * @author Philip Aston
 */
final class ThreadContextImplementation implements ThreadContext {

	private final ListenerSupport<ThreadLifeCycleListener> m_threadLifeCycleListeners = new ListenerSupport<ThreadLifeCycleListener>();

	private final DispatchContextStack m_dispatchContextStack = new DispatchContextStack();

	private final int m_threadNumber;
	private final Marker m_threadMarker;
	private final DispatchResultReporter m_dispatchResultReporter;

	private SSLContextFactory m_sslContextFactory;

	private boolean m_delayReports;

	private DispatchContext m_pendingDispatchContext;

	private StatisticsForTest m_statisticsForLastTest;

	private Marker m_runMarker;
	private int m_runNumber = -1;

	private Marker m_testMarker;

	private volatile boolean m_shutdown;
	private boolean m_shutdownReported;

	public ThreadContextImplementation(GrinderProperties properties,
			StatisticsServices statisticsServices, int threadNumber,
			Logger dataLogger) throws EngineException {

		m_threadNumber = threadNumber;
		m_threadMarker = MarkerFactory.getMarker("thread-" + threadNumber);

		// Undocumented property. Added so Tom Barnes can investigate overhead
		// of data logging.
		if (properties.getBoolean("grinder.logData", true)) {
			final ThreadDataLogger threadDataLogger = new ThreadDataLogger(
					dataLogger, statisticsServices.getDetailStatisticsView()
							.getExpressionViews(), m_threadNumber);

			m_dispatchResultReporter = new DispatchResultReporter() {
				public void report(Test test, long startTime,
						StatisticsSet statistics) {
					threadDataLogger.report(getRunNumber(), test, startTime,
							statistics);
				}
			};
		} else {
			m_dispatchResultReporter = new DispatchResultReporter() {
				public void report(Test test, long startTime,
						StatisticsSet statistics) {
					// Null reporter.
				}
			};
		}

		registerThreadLifeCycleListener(new SkeletonThreadLifeCycleListener() {
			public void endRun() {
				reportPendingDispatchContext();
			}
		});
	}

	public int getThreadNumber() {
		return m_threadNumber;
	}

	public int getRunNumber() {
		return m_runNumber;
	}

	@Override
	public void setCurrentRunNumber(int run) {
		if (m_runMarker != null) {
			m_threadMarker.remove(m_runMarker);
			MarkerFactory.getIMarkerFactory().detachMarker(
					m_runMarker.getName());
		}

		if (run != -1) {
			m_runMarker = MarkerFactory.getMarker("run-" + run);
			m_threadMarker.add(m_runMarker);
		}

		m_runNumber = run;
	}

	/** Package scope for unit tests. */
	void setTestLogMarker(Marker marker) {
		if (m_testMarker != null) {
			m_threadMarker.remove(m_testMarker);
		}

		m_testMarker = marker;

		if (marker != null) {
			m_threadMarker.add(marker);
		}
	}

	public SSLContextFactory getThreadSSLContextFactory() {
		return m_sslContextFactory;
	}

	public void setThreadSSLContextFactory(SSLContextFactory sslContextFactory) {
		m_sslContextFactory = sslContextFactory;
	}

	public DispatchResultReporter getDispatchResultReporter() {
		return m_dispatchResultReporter;
	}

	public void registerThreadLifeCycleListener(ThreadLifeCycleListener listener) {
		m_threadLifeCycleListeners.add(listener);
	}

	public void removeThreadLifeCycleListener(ThreadLifeCycleListener listener) {
		m_threadLifeCycleListeners.remove(listener);
	}

	public void fireBeginThreadEvent() {
		m_threadLifeCycleListeners
				.apply(new Informer<ThreadLifeCycleListener>() {
					public void inform(ThreadLifeCycleListener l) {
						l.beginThread();
					}
				});
	}

	public void fireBeginRunEvent() {
		m_threadLifeCycleListeners
				.apply(new Informer<ThreadLifeCycleListener>() {
					public void inform(ThreadLifeCycleListener l) {
						l.beginRun();
					}
				});
	}

	public void fireEndRunEvent() {
		m_threadLifeCycleListeners
				.apply(new Informer<ThreadLifeCycleListener>() {
					public void inform(ThreadLifeCycleListener l) {
						l.endRun();
					}
				});
	}

	public void fireBeginShutdownEvent() {
		m_threadLifeCycleListeners
				.apply(new Informer<ThreadLifeCycleListener>() {
					public void inform(ThreadLifeCycleListener l) {
						l.beginShutdown();
					}
				});
	}

	public void fireEndThreadEvent() {
		m_threadLifeCycleListeners
				.apply(new Informer<ThreadLifeCycleListener>() {
					public void inform(ThreadLifeCycleListener l) {
						l.endThread();
					}
				});

	}

	public void pushDispatchContext(DispatchContext dispatchContext)
			throws ShutdownException {

		if (m_shutdown) {
			// As soon as we're shutdown, we disable the instrumentation. This
			// avoids reporting of misleading test failures.

			// We only throw ShutdownException from pushDispatchContext. A test
			// that
			// was in-flight at the time of shutdown will complete normally
			// unless
			// the thread attempts to start a nested test.
			m_shutdownReported = true;
			throw new ShutdownException("Thread has been shut down");
		}

		reportPendingDispatchContext();

		setTestLogMarker(dispatchContext.getLogMarker());

		final DispatchContext existingContext = m_dispatchContextStack
				.peekTop();

		if (existingContext != null) {
			existingContext.setHasNestedContexts();
		}

		m_dispatchContextStack.push(dispatchContext);
	}

	public void popDispatchContext() {
		if (m_shutdownReported) {
			return;
		}

		final DispatchContext dispatchContext = m_dispatchContextStack.pop();

		if (dispatchContext == null) {
			throw new AssertionError("DispatchContext stack unexpectedly empty");
		}

		final DispatchContext parentDispatchContext = m_dispatchContextStack
				.peekTop();

		if (parentDispatchContext != null) {
			parentDispatchContext.getPauseTimer().add(
					dispatchContext.getPauseTimer());
		}

		m_statisticsForLastTest = dispatchContext.getStatisticsForTest();

		// Flush any pending report created by an inner test.
		reportPendingDispatchContext();

		if (m_delayReports) {
			m_pendingDispatchContext = dispatchContext;
		} else {
			try {
				dispatchContext.report();
			} catch (DispatchStateException e) {
				throw new AssertionError(e);
			}
		}

		setTestLogMarker(null);
	}

	public StatisticsForTest getStatisticsForCurrentTest() {
		final DispatchContext dispatchContext = m_dispatchContextStack
				.peekTop();

		if (dispatchContext == null) {
			return null;
		}

		return dispatchContext.getStatisticsForTest();
	}

	public StatisticsForTest getStatisticsForLastTest() {
		return m_statisticsForLastTest;
	}

	public void setDelayReports(boolean b) {
		if (!b) {
			reportPendingDispatchContext();
		}

		m_delayReports = b;
	}

	public void reportPendingDispatchContext() {
		if (m_pendingDispatchContext != null) {
			try {
				m_pendingDispatchContext.report();
			} catch (DispatchStateException e) {
				throw new AssertionError(e);
			}

			m_pendingDispatchContext = null;
		}
	}

	public void pauseClock() {
		final DispatchContext dispatchContext = m_dispatchContextStack
				.peekTop();

		if (dispatchContext != null) {
			dispatchContext.getPauseTimer().start();
		}
	}

	public void resumeClock() {
		final DispatchContext dispatchContext = m_dispatchContextStack
				.peekTop();

		if (dispatchContext != null) {
			dispatchContext.getPauseTimer().stop();
		}
	}

	private static final class DispatchContextStack {
		private final List<DispatchContext> m_stack = new ArrayList<DispatchContext>();

		public void push(DispatchContext dispatchContext) {
			m_stack.add(dispatchContext);
		}

		public DispatchContext pop() {
			final int size = m_stack.size();

			if (size == 0) {
				return null;
			}

			return m_stack.remove(size - 1);
		}

		public DispatchContext peekTop() {
			final int size = m_stack.size();

			if (size == 0) {
				return null;
			}

			return m_stack.get(size - 1);
		}
	}

	public void shutdown() {
		MarkerFactory.getIMarkerFactory()
				.detachMarker(m_threadMarker.getName());
		m_shutdown = true;
	}

	@Override
	public Marker getLogMarker() {
		return m_threadMarker;
	}
}