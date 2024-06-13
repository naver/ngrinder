package org.ngrinder.script.service;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.script.model.FileEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.constructor.ConstructorException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitHubFileEntryServiceTest extends AbstractNGrinderTransactionalTest {
	@Autowired
	private GitHubFileEntryService gitHubFileEntryService;

	@Test
	public void testValidateInvalidConfigNameLength() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent(
				"name: My Long Long Long Long Long Long Github Config Name\n" +
						"owner: naver\n" +
						"repo: ngrinder\n" +
						"access-token: e1a47e652762b60a...3ddc0713b07g13k\n"
		);
		fileEntry.setRevision(-1L);

		try {
			gitHubFileEntryService.validate(fileEntry);
		} catch (Exception e) {
			assertTrue(e instanceof NGrinderRuntimeException);
			assertTrue(e.getMessage().contains("Configuration name must be shorter than"));
		}
	}

	@Test
	public void testValidateInvalidYamlValue() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent(
				"!!com.sun.rowset.JdbcRowSetImpl\n " +
						"dataSourceName: rmi://127.0.0.1:13243/jmxrmi\n " +
						"autoCommit: true"
		);
		fileEntry.setRevision(-1L);

		try {
			gitHubFileEntryService.validate(fileEntry);
		} catch (Exception e) {
			assertTrue(e instanceof ConstructorException);
			assertTrue(e.getMessage().contains("could not determine a constructor for the tag"));
		}
	}

	@Test
	public void testValidateInvalidYamlValue2() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent(
				"some_var: !!javax.script.ScriptEngineManager " +
						"[!!java.net.URLClassLoader [[!!java.net.URL [\"http://localhost:8080\"]]]]"
		);
		fileEntry.setRevision(-1L);

		try {
			gitHubFileEntryService.validate(fileEntry);
		} catch (Exception e) {
			assertTrue(e instanceof ConstructorException);
			assertTrue(e.getMessage().contains("could not determine a constructor for the tag"));
		}
	}
}
