/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.http;

import net.grinder.plugin.http.HTTPPlugin;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;

public class SummarizeRedirectStrategy extends DefaultRedirectStrategy {

	@Override
	public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
		summarize(request, response);
		return super.isRedirected(request, response, context);
	}

	private void summarize(HttpRequest request, HttpResponse response) {
		try {
			getScriptLogger().info("{} -> {} {}", request.getUri(), response.getCode(), response.getReasonPhrase());
		} catch (Exception e) {
			// noop
		}
	}

	private Logger getScriptLogger() {
		return HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getLogger();
	}
}
