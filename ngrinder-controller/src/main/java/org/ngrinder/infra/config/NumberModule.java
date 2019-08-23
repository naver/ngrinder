package org.ngrinder.infra.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class NumberModule extends SimpleModule {
	public NumberModule() {
		this.addSerializer(float.class, new FloatSerializer());
		this.addSerializer(double.class, new DoubleSerializer());
	}

	private static class FloatSerializer extends JsonSerializer<Float> {
		@Override
		public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (value.isNaN()) {
				gen.writeNull();
			} else {
				gen.writeNumber(value);
			}
		}
	}

	private static class DoubleSerializer extends JsonSerializer<Double> {
		@Override
		public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (value.isNaN()) {
				gen.writeNull();
			} else {
				gen.writeNumber(value);
			}
		}
	}
}
