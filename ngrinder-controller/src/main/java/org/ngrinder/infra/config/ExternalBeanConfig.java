package org.ngrinder.infra.config;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Profile("production")
@Configuration
public class ExternalBeanConfig {

	@Bean
	public LocalScriptTestDriveService localScriptTestDriveService() {
		return new LocalScriptTestDriveService(new File(Config.getCurrentLibPath()).getParentFile());
	}
}
