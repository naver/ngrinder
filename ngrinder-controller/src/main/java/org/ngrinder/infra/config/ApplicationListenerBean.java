package org.ngrinder.infra.config;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.model.Home;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
public class ApplicationListenerBean implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private Config config;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Home exHome = config.getExHome();
		if (exHome.exists()) {
			FileUtils.deleteQuietly(exHome.getSubFile("shutdown.lock"));
		}
	}
}