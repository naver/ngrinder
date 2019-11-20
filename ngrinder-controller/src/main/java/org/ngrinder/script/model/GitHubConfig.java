package org.ngrinder.script.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.ngrinder.common.exception.NGrinderRuntimeException;

import java.io.IOException;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = GitHubConfig.GitHubConfigDeserializer.class)
public class GitHubConfig {
	private String name;
	private String owner;
	private String repo;
	private String accessToken;
	private String baseUrl;
	private String revision;

	public static class GitHubConfigDeserializer extends JsonDeserializer<GitHubConfig> {

		@Override
		public GitHubConfig deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			ObjectCodec objectCodec = jsonParser.getCodec();
			JsonNode jsonNode = objectCodec.readTree(jsonParser);

			try {
				return new GitHubConfig(
					jsonNode.get("name").asText(),
					defaultIfNull(jsonNode.get("owner"), ""),
					defaultIfNull(jsonNode.get("repo"), ""),
					defaultIfNull(jsonNode.get("access-token"), ""),
					defaultIfNull(jsonNode.get("base-url"), ""),
					defaultIfNull(jsonNode.get("revision"), "-1"));
			} catch (RuntimeException e) {
				throw new NGrinderRuntimeException("Required field 'name' is missing. please check your .gitconfig.yml");
			}
		}

		private String defaultIfNull(JsonNode jsonNode, String defaultValue) {
			return jsonNode == null ? defaultValue : jsonNode.asText(defaultValue);
		}
	}
}
