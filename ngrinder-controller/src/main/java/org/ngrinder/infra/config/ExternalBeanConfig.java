package org.ngrinder.infra.config;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ExternalBeanConfig {

	@Bean
	public LocalScriptTestDriveService localScriptTestDriveService() {
		return new LocalScriptTestDriveService(new File(Config.getCurrentLibPath()).getParentFile());
	}
}
