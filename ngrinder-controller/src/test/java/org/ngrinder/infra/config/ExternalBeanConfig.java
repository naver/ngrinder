package org.ngrinder.infra.config;

import org.ngrinder.script.service.MockLocalScriptTestDriveService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Profile("unit-test")
@Configuration
public class ExternalBeanConfig {

	@Bean
	public MockLocalScriptTestDriveService mockLocalScriptTestDriveService() {
		return new MockLocalScriptTestDriveService(new File(Config.getCurrentLibPath()).getParentFile());
	}
}
