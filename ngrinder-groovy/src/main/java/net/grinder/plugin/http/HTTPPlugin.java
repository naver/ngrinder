// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000 - 2008 Philip Aston
// Copyright (C) 2004 Bertrand Ave
// Copyright (C) 2004 John Stanford White
// Copyright (C) 2004 Calum Fitzgerald
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

package net.grinder.plugin.http;

import HTTPClient.CookieModule;
import HTTPClient.DefaultAuthHandler;
import HTTPClient.HTTPConnection;

import net.grinder.common.GrinderException;
import net.grinder.common.SSLContextFactory;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.PluginProcessContext;
import net.grinder.plugininterface.PluginRegistry;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.plugininterface.PluginThreadListener;
import net.grinder.script.Grinder;
import net.grinder.script.Statistics;
import net.grinder.statistics.StatisticsIndexMap;
import net.grinder.util.Sleeper;
import net.grinder.util.SleeperImplementation;


/**
 * HTTP plug-in.
 *
 * @author Paco Gomez
 * @author Philip Aston
 * @author Bertrand Ave
 */
public class HTTPPlugin implements GrinderPlugin {

	private static final HTTPPlugin s_singleton = new HTTPPlugin();

	static {
		try {
			PluginRegistry.getInstance().register(s_singleton);
		}
		catch (GrinderException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Static package scope accessor for the initialised instance of the
	 * plug-in.
	 *
	 * @return The plug-in instance.
	 */
	static final HTTPPlugin getPlugin() {
		return s_singleton;
	}

	private PluginProcessContext m_pluginProcessContext;
	private SSLContextFactory m_sslContextFactory;
	private Sleeper m_slowClientSleeper;

	final PluginProcessContext getPluginProcessContext() {
		return m_pluginProcessContext;
	}

	/**
	 * Called by the PluginRegistry when the plug-in is first registered.
	 *
	 * @param processContext The plug-in process context.
	 * @exception PluginException if an error occurs.
	 */
	public void initialize(PluginProcessContext processContext)
		throws PluginException {

		m_pluginProcessContext = processContext;

		final Grinder.ScriptContext scriptContext =
			processContext.getScriptContext();

		m_sslContextFactory = scriptContext.getSSLControl();

		m_slowClientSleeper =
			new SleeperImplementation(
				m_pluginProcessContext.getTimeAuthority(), null, 1, 0);

		// Remove standard HTTPClient modules which we don't want. We load
		// HTTPClient modules dynamically as we don't have public access.
		try {
			// Don't want to retry requests.
			HTTPConnection.removeDefaultModule(
				Class.forName("HTTPClient.RetryModule"));
		}
		catch (ClassNotFoundException e) {
			throw new PluginException("Could not load HTTPClient modules", e);
		}

		// Turn off cookie permission checks.
		CookieModule.setCookiePolicyHandler(null);

		// Turn off authorisation UI.
		DefaultAuthHandler.setAuthorizationPrompter(null);

		// Register custom statistics.
		try {

			final Statistics statistics = scriptContext.getStatistics();

			statistics.registerDataLogExpression(
				"HTTP response code",
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_STATUS_KEY);

			statistics.registerDataLogExpression(
				"HTTP response length",
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY);

			statistics.registerDataLogExpression(
				"HTTP response errors",
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_ERRORS_KEY);

			statistics.registerDataLogExpression(
				"Time to resolve host",
				StatisticsIndexMap.HTTP_PLUGIN_DNS_TIME_KEY);

			statistics.registerDataLogExpression(
				"Time to establish connection",
				StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY);

			statistics.registerDataLogExpression(
				"Time to first byte",
				StatisticsIndexMap.HTTP_PLUGIN_FIRST_BYTE_TIME_KEY);

			statistics.registerSummaryExpression(
				"Mean response length",
				"(/ " + StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY +
					" (+ (count timedTests) untimedTests))");

			statistics.registerSummaryExpression(
				"Response bytes per second",
				"(* 1000 (/ " + StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY +
					" period))");

			statistics.registerSummaryExpression(
				"Response errors",
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_ERRORS_KEY);

			statistics.registerSummaryExpression(
				"Mean time to resolve host",
				"(/ " + StatisticsIndexMap.HTTP_PLUGIN_DNS_TIME_KEY +
					" (+ (count timedTests) untimedTests))");

			statistics.registerSummaryExpression(
				"Mean time to establish connection",
				"(/ " + StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY +
					" (+ (count timedTests) untimedTests))");

			statistics.registerSummaryExpression(
				"Mean time to first byte",
				"(/ " + StatisticsIndexMap.HTTP_PLUGIN_FIRST_BYTE_TIME_KEY +
					" (+ (count timedTests) untimedTests))");
		}
		catch (GrinderException e) {
			throw new PluginException("Could not register custom statistics", e);
		}
	}

	/**
	 * Called by the engine to obtain a new PluginThreadListener.
	 *
	 * @param threadContext The plug-in thread context.
	 * @return The new plug-in thread listener.
	 * @exception PluginException if an error occurs.
	 */
	public PluginThreadListener createThreadListener(
		PluginThreadContext threadContext) throws PluginException {

		return new HTTPPluginThreadState(threadContext,
			m_sslContextFactory,
			m_slowClientSleeper,
			m_pluginProcessContext.getTimeAuthority());
	}
}
