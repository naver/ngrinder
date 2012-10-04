package org.ngrinder.operation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.operation.cotroller.LogMonitorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class LogMonitorControllerTest extends AbstractNGrinderTransactionalTest{
	public static final Logger LOGGER = LoggerFactory.getLogger(LogMonitorControllerTest.class);
	@Autowired
	private LogMonitorController logMonitorController;
	
	@Test
	public void testLogMonitorController() {
		CoreLogger.LOGGER.info("Core Logger");
		LOGGER.debug("TEST TEST");
		sleep(3000);
		assertThat(getLastMessage(), not(containsString("TEST TEST")));
		assertThat(getLastMessage(), containsString("Core Logger"));

		logMonitorController.enableVerbose(true);
		LOGGER.debug("TEST TEST");
		sleep(3000);
		assertThat(getLastMessage(), containsString("TEST TEST"));
		
	}

	private String getLastMessage() {
		HttpEntity<String> lastLog = logMonitorController.getLastLog();
		JsonParser parser = new JsonParser();
		JsonElement parse = parser.parse(lastLog.getBody());
		String message = parse.getAsJsonObject().get("log").getAsString();
		return message;
	}
}
