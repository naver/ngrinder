package org.ngrinder.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.NumberModule;

import java.io.IOException;

public class JsonUtils {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerModule(new NumberModule());
	}

	public static String serialize(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new NGrinderRuntimeException("Fail to serialize", e);
		}
	}

	public static <T> T deserialize(String jsonString, Class<T> clazz) {
		try {
			return objectMapper.readValue(jsonString, clazz);
		} catch (IOException e) {
			throw new NGrinderRuntimeException("Fail to deserialize", e);
		}
	}

	public static <T> T deserialize(String jsonString, TypeReference typeReference) {
		try {
			return objectMapper.readValue(jsonString, typeReference);
		} catch (IOException e) {
			throw new NGrinderRuntimeException("Fail to deserialize", e);
		}
	}
}
