// Copyright (C) 2002 - 2009 Philip Aston
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

import HTTPClient.*;
import net.grinder.common.SSLContextFactory;
import net.grinder.common.SSLContextFactory.SSLContextFactoryException;
import net.grinder.common.SkeletonThreadLifeCycleListener;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.plugininterface.PluginThreadListener;
import net.grinder.util.Sleeper;
import net.grinder.util.TimeAuthority;
import org.ngrinder.http.HTTPRequester;
import org.ngrinder.http.cookie.CookieManager;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;


/**
 * HTTP plug-in thread state. (modified for nGrinder)
 *
 * @author Philip Aston
 */
class HTTPPluginThreadState extends SkeletonThreadLifeCycleListener implements PluginThreadListener {

	private final PluginThreadContext m_threadContext;
	private final SSLContextFactory m_sslContextFactory;

	private final Map<URI, HTTPConnectionWrapper> m_httpConnectionWrappers = new HashMap<URI, HTTPConnectionWrapper>();
	private HTTPResponse m_lastResponse;
	private final Sleeper m_slowClientSleeper;
	private final TimeAuthorityAdapter m_timeAuthority;

	private final String connectionResetProperty;

	HTTPPluginThreadState(PluginThreadContext threadContext,
						  SSLContextFactory sslContextFactory,
						  Sleeper slowClientSleeper,
						  TimeAuthority timeAuthority) throws PluginException {
		m_threadContext = threadContext;
		m_sslContextFactory = sslContextFactory;
		m_slowClientSleeper = slowClientSleeper;
		m_timeAuthority = new TimeAuthorityAdapter(timeAuthority);
		connectionResetProperty = getProperty("ngrinder.connection.reset.on.each.test.run", "");

	}

	public PluginThreadContext getThreadContext() {
		return m_threadContext;
	}

	public HTTPConnectionWrapper getConnectionWrapper(URI uri)
		throws ParseException,
		ProtocolNotSuppException,
		SSLContextFactoryException {

		final URI keyURI =
			new URI(uri.getScheme(), uri.getHost(), uri.getPort(), "");

		final HTTPConnectionWrapper existingConnectionWrapper = m_httpConnectionWrappers.get(keyURI);

		if (existingConnectionWrapper != null) {
			return existingConnectionWrapper;
		}

		final HTTPPluginConnectionDefaults connectionDefaults = HTTPPluginConnectionDefaults.getConnectionDefaults();

		final HTTPConnection httpConnection = new HTTPConnection(uri);
		httpConnection.setContext(this);

		if ("https".equals(uri.getScheme())) {
			httpConnection.setSSLSocketFactory(m_sslContextFactory.getSSLContext().getSocketFactory());
		}

		httpConnection.setTimeAuthority(m_timeAuthority);

		final HTTPConnectionWrapper newConnectionWrapper = new HTTPConnectionWrapper(
			httpConnection,
			connectionDefaults,
			m_slowClientSleeper);

		m_httpConnectionWrappers.put(keyURI, newConnectionWrapper);

		return newConnectionWrapper;
	}

	public void beginRun() {
		// Discard our cookies.
		CookieModule.discardAllCookies(this);
		CookieManager.reset();

		if (connectionResetProperty.isEmpty()) {
			return;
		}

		// Close connections from previous run.
		for (HTTPConnectionWrapper connection : m_httpConnectionWrappers.values()) {
			connection.close();
		}

		m_httpConnectionWrappers.clear();
		
		// Close connections from previous run.
		HTTPRequester.reset();
	}

	public void setLastResponse(HTTPResponse lastResponse) {
		m_lastResponse = lastResponse;
	}

	public HTTPResponse getLastResponse() {
		return m_lastResponse;
	}

	private static final class TimeAuthorityAdapter implements
		HTTPClient.HTTPConnection.TimeAuthority {

		private final TimeAuthority m_timeAuthority;

		public TimeAuthorityAdapter(TimeAuthority timeAuthority) {
			m_timeAuthority = timeAuthority;
		}

		public long getTimeInMilliseconds() {
			return m_timeAuthority.getTimeInMilliseconds();
		}
	}
}

