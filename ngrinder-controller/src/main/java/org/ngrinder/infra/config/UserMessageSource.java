package org.ngrinder.infra.config;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.ngrinder.common.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * Custom user defined message source handler. User can defines its own message translations in
 * ${NGRINDER_HOME}/messages/messages_{langcode}.properties.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Component("userMessageSource")
public class UserMessageSource extends AbstractMessageSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserMessageSource.class);
	@Autowired
	private Config config;

	// It's safe to use hash map in multi thread here. because it's read only.
	private Map<LocaleAndCode, MessageFormat> langMessageMap = Maps.newHashMap();

	/**
	 * Message key holder with local and code.
	 * 
	 * @author JunHo Yoon
	 * @since 3.1
	 */
	static class LocaleAndCode {
		
		/**
		 * Constructor.
		 * 
		 * @param locale locale
		 * @param code code
		 */
		public LocaleAndCode(String locale, String code) {
			this.locale = locale;
			this.code = code;
		}

		private String locale;
		private String code;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result + ((locale == null) ? 0 : locale.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

	}

	/**
	 * Refresh i18n messages.
	 * @throws IOException IO exception
	 */
	public void refresh() throws IOException {
		init();
	}

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		File messagesDirectory = config.getHome().getMessagesDirectory();
		if (messagesDirectory.exists()) {
			for (String each : Locale.getISOLanguages()) {
				File file = new File(messagesDirectory, "messages_" + each + ".properties");
				if (file.exists()) {
					try {
						byte[] propByte = FileUtils.readFileToByteArray(file);
						String propString = EncodingUtils.getAutoDecodedString(propByte, "UTF-8");
						Properties prop = new Properties();
						prop.load(new StringReader(propString));
						for (Map.Entry<Object, Object> eachEntry : prop.entrySet()) {
							langMessageMap.put(new LocaleAndCode(each, (String) eachEntry.getKey()), new MessageFormat(
											(String) eachEntry.getValue()));
						}
					} catch (Exception e) {
						LOGGER.error("Error while loading {}", file.getAbsolutePath(), e);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.support.AbstractMessageSource#resolveCode(java.lang.String,
	 * java.util.Locale)
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		MessageFormat resolved = langMessageMap.get(new LocaleAndCode(locale.getLanguage(), code));
		return resolved == null ? langMessageMap.get(new LocaleAndCode("en", code)) : resolved;
	}
}
