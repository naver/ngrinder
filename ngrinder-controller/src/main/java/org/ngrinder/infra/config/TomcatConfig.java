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
package org.ngrinder.infra.config;

import lombok.Setter;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Setter
@Configuration
@ConfigurationProperties(prefix = "tomcat.ajp")
public class TomcatConfig {

	private int port;
	private String secret;
	private String protocol;
	private boolean enable;

	@Bean
	public ServletWebServerFactory createTomcat() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		if (enable) {
			tomcat.addAdditionalTomcatConnectors(createAjpConnector());
		}
		return tomcat;
	}

	private Connector createAjpConnector() {
		Connector ajpConnector = new Connector(protocol);
		ajpConnector.setPort(port);
		if (isEmpty(secret)) {
			((AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler()).setSecretRequired(false);
		} else {
			((AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler()).setSecret(secret);
		}
		return ajpConnector;
	}
}
