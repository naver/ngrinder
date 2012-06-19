package com.nhncorp.ngrinder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemProperties {

	private static final Logger LOG = LoggerFactory.getLogger(SystemProperties.class);

	public static final String NGRINDER_VER;
	public static final String GRINDER_VER;

	static {
		Properties prop = new Properties();
		try {
			String classPath = SystemProperties.class.getResource("/").getPath();
			prop.load(new FileInputStream(new File(classPath + "data.properties")));
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		// ApplicationContext context = AppContext.getCtx();
		// Properties prop = (Properties) context.getBean("dataProps");

		NGRINDER_VER = prop.getProperty("ngrinder.version");
		GRINDER_VER = prop.getProperty("grinder.version");

	}

}
