package org.ngrinder.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ngrinder.common.exception.NGrinderRuntimeException;

import java.io.IOException;

public class UncheckedObjectMapper extends ObjectMapper {
	@Override
	public String writeValueAsString(Object value) {
		try {
			return super.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	@Override
	public <T> T readValue(String content, Class<T> valueType) {
		try {
			return super.readValue(content, valueType);
		} catch (IOException e) {
			throw new NGrinderRuntimeException(e);
		}
	}
}
