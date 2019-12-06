package org.ngrinder.infra.config;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Properties;

@Component
public class ExportableMessageSource extends ReloadableResourceBundleMessageSource {
	public Properties getAllProperties(Locale locale) {
		clearCacheIncludingAncestors();
		PropertiesHolder propertiesHolder = getMergedProperties(locale);
		Properties properties = propertiesHolder.getProperties();

		return properties;
	}
}
