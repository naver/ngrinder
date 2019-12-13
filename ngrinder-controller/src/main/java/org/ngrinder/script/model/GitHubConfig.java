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
	private String userId;
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
					jsonNode.get("owner").asText(),
					jsonNode.get("repo").asText(),
					jsonNode.get("user-id").asText(),
					jsonNode.get("access-token").asText(),
					defaultIfNull(jsonNode.get("base-url"), ""),
					defaultIfNull(jsonNode.get("revision"), "-1"));
			} catch (Exception e) {
				throw new NGrinderRuntimeException("Required field(name, owner, repo, user-id, access-token) is missing.<br>Please check your .gitconfig.yml", e);
			}
		}

		private String defaultIfNull(JsonNode jsonNode, String defaultValue) {
			return jsonNode == null ? defaultValue : jsonNode.asText(defaultValue);
		}
	}
}
