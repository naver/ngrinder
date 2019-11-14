package org.ngrinder.script.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.io.IOException;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = GitConfig.GitConfigDeserializer.class)
public class GitConfig {
	private String owner;
	private String repo;
	private String accessToken;
	private String baseUrl;

	public static class GitConfigDeserializer extends JsonDeserializer<GitConfig> {

		@Override
		public GitConfig deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			ObjectCodec objectCodec = jsonParser.getCodec();
			JsonNode jsonNode = objectCodec.readTree(jsonParser);

			return new GitConfig(
				defaultIfNull(jsonNode.get("owner"), ""),
				defaultIfNull(jsonNode.get("repo"), ""),
				defaultIfNull(jsonNode.get("access-token"), ""),
				defaultIfNull(jsonNode.get("base-url"), ""));
		}

		private String defaultIfNull(JsonNode jsonNode, String defaultValue) {
			return jsonNode == null ? defaultValue : jsonNode.asText(defaultValue);
		}
	}
}
