package com.nhncorp.ngrinder.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarPathTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(JarPathTest.class);

	@Test
	public void testGetJatPath (){

		LOG.debug("just a msg");
		String logClassName = LOG.getClass().getName();
		LOG.debug("find class:{}", logClassName);
		String logClassNamePath = logClassName.replace(".", "/");
		logClassNamePath = logClassNamePath + ".class";
		LOG.debug("find path:{}", logClassNamePath);
		String fullPath = this.getClass().getClassLoader().getResource(logClassNamePath).toString();
		LOG.debug("full path:{}", fullPath);

		String webLibPath = fullPath.substring(0, fullPath.indexOf(logClassNamePath));
		LOG.debug("path:{}", webLibPath);
	}
}
