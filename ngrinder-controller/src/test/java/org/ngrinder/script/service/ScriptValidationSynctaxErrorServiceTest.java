package org.ngrinder.script.service;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ScriptValidationSynctaxErrorServiceTest {
	public ScriptValidationService scriptValidationService = new ScriptValidationService();

	@Test
	public void testSyntaxError() {
		assertThat(scriptValidationService.checkSyntaxErrors("print 'HELL'.."),
				notNullValue());
		assertThat(scriptValidationService.checkSyntaxErrors("print 'HELL'"),
				nullValue());

	}
}
