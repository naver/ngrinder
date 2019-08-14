package org.ngrinder.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonUtils {
	private static ObjectMapper objectMapper;

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		JsonUtils.objectMapper = objectMapper;
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
