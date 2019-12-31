package org.ngrinder.script.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.validator.routines.UrlValidator;
import org.ngrinder.common.exception.NGrinderRuntimeException;

import java.io.IOException;

import static org.apache.commons.validator.routines.UrlValidator.getInstance;

@Getter
@Setter
@Builder
@ToString
@JsonDeserialize(using = GitHubConfig.GitHubConfigDeserializer.class)
public class GitHubConfig {
	private String name;
	private String owner;
	private String repo;
	private String userId;
	private String accessToken;
	private String branch;
	private String baseUrl;
	private String revision;
	private String scriptRoot;

	public static class GitHubConfigDeserializer extends JsonDeserializer<GitHubConfig> {

		@Override
		public GitHubConfig deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			ObjectCodec objectCodec = jsonParser.getCodec();
			JsonNode jsonNode = objectCodec.readTree(jsonParser);
			UrlValidator urlValidator = getInstance();

			String baseUrl = defaultIfNull(jsonNode.get("base-url"), "https://api.github.com");
			if (!urlValidator.isValid(baseUrl)) {
				throw new NGrinderRuntimeException("Field 'base-url' is invalid.<br>Please check your .gitconfig.yml");
			}

			try {
				return GitHubConfig.builder()
					.name(jsonNode.get("name").asText())
					.owner(jsonNode.get("owner").asText())
					.repo(jsonNode.get("repo").asText())
					.userId(jsonNode.get("user-id").asText())
					.accessToken(jsonNode.get("access-token").asText())
					.branch(defaultIfNull(jsonNode.get("branch"), ""))
					.baseUrl(baseUrl)
					.revision(defaultIfNull(jsonNode.get("revision"), "-1"))
					.scriptRoot(defaultIfNull(jsonNode.get("script-root"), ""))
					.build();
			} catch (Exception e) {
				throw new NGrinderRuntimeException("Required field(name, owner, repo, user-id, access-token) is missing.<br>Please check your .gitconfig.yml", e);
			}
		}

		private String defaultIfNull(JsonNode jsonNode, String defaultValue) {
			return jsonNode == null ? defaultValue : jsonNode.asText(defaultValue);
		}
	}
}
