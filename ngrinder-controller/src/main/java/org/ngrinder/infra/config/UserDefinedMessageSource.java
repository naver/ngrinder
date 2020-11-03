package org.ngrinder.infra.config;

import java.io.File;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.ngrinder.common.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

/**
 * Custom user defined message source handler. User can defines its own message translations in
 * ${NGRINDER_HOME}/messages/messages_{langcode}.properties.
 *
 * @since 3.1
 */
@Component("userMessageSource")
@RequiredArgsConstructor
public class UserDefinedMessageSource extends AbstractMessageSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDefinedMessageSource.class);

	private final Config config;

	// It's safe to use hash map in multi thread here. because it's read only.
	private Map<LocaleAndCode, MessageFormat> langMessageMap;

	@Getter
	private Map<String, Map<String, String>> messageSourcesByLocale;

	/**
	 * Message key holder with local and code.
	 *
	 * @since 3.1
	 */
	static class LocaleAndCode {

		private final String locale;
		private final String code;

		public LocaleAndCode(String locale, String code) {
			this.locale = locale;
			this.code = code;
		}

		@Override
		public int hashCode() {
			return Objects.hash(code, locale);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		langMessageMap = getLangMessageMap();
		messageSourcesByLocale = getMessageSources();
	}

	private Map<LocaleAndCode, MessageFormat> getLangMessageMap() {
		Map<LocaleAndCode, MessageFormat> map = new HashMap<>();

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
							map.put(new LocaleAndCode(each, (String) eachEntry.getKey()), new MessageFormat(
								(String) eachEntry.getValue()));
						}
					} catch (Exception e) {
						LOGGER.error("Error while loading {}", file.getAbsolutePath(), e);
					}
				}
			}
		}

		return map;
	}

	private Map<String, Map<String, String>> getMessageSources() {
		return langMessageMap
			.keySet()
			.stream()
			.map(localeAndCode -> localeAndCode.locale)
			.distinct()
			.collect(Collectors.toMap(Function.identity(), this::getMessageMap));
	}

	private Map<String, String> getMessageMap(String locale) {
		return langMessageMap
			.entrySet()
			.stream()
			.filter(entry -> entry.getKey().locale.equals(locale))
			.collect(Collectors.toMap(entry -> entry.getKey().code, entry -> entry.getValue().toPattern()));
	}

	@SuppressWarnings("NullableProblems")
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		MessageFormat resolved = langMessageMap.get(new LocaleAndCode(locale.getLanguage(), code));
		return resolved == null ? langMessageMap.get(new LocaleAndCode("en", code)) : resolved;
	}
}
