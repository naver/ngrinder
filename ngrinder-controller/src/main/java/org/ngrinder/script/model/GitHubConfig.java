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
import org.ngrinder.common.exception.InvalidGitHubConfigurationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import static java.util.Objects.hash;
import static org.apache.commons.validator.routines.UrlValidator.getInstance;
import static org.ngrinder.script.model.GitHubConfigField.*;

@Getter
@Setter
@Builder
@ToString
@JsonDeserialize(using = GitHubConfig.GitHubConfigDeserializer.class)
public class GitHubConfig {
	public static final int CONFIG_NAME_MAX_LENGTH = 40;

	private String name;
	private String owner;
	private String repo;
	private String accessToken;
	private String branch;
	private String baseUrl;
	private String revision;
	private String scriptRoot;

	@Override
	public int hashCode() {
		return hash(name);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null) {
			return false;
		}

		if (this.getClass() != object.getClass()) {
			return false;
		}

		GitHubConfig gitHubConfig = (GitHubConfig) object;
		return name.equals(gitHubConfig.name);
	}

	public static class GitHubConfigDeserializer extends JsonDeserializer<GitHubConfig> {

		@Override
		public GitHubConfig deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			ObjectCodec objectCodec = jsonParser.getCodec();
			JsonNode jsonNode = objectCodec.readTree(jsonParser);

			checkUnsupportedField(jsonNode);

			String baseUrl = defaultIfNull(jsonNode.get(BASE_URL.getValue()), "");
			checkUrlFormat(baseUrl);

			try {
				return GitHubConfig.builder()
					.name(jsonNode.get(NAME.getValue()).asText())
					.owner(jsonNode.get(OWNER.getValue()).asText())
					.repo(jsonNode.get(REPO.getValue()).asText())
					.accessToken(jsonNode.get(ACCESS_TOKEN.getValue()).asText())
					.branch(defaultIfNull(jsonNode.get(BRANCH.getValue()), null))
					.baseUrl(baseUrl)
					.revision(defaultIfNull(jsonNode.get(REVISION.getValue()), "-1"))
					.scriptRoot(defaultIfNull(jsonNode.get(SCRIPT_ROOT.getValue()), ""))
					.build();
			} catch (NullPointerException e) {
				throw new InvalidGitHubConfigurationException("Some of required fields(name, owner, repo, access-token) are missing.\n" +
					"Please check your .gitconfig.yml", e);
			}
		}

		private String defaultIfNull(JsonNode jsonNode, String defaultValue) {
			return jsonNode == null ? defaultValue : jsonNode.asText(defaultValue);
		}

		/**
		 * Check base-url configuration value conforms to url format.
		 *
		 * @throws InvalidGitHubConfigurationException occurs when Not conform to url format.
		 * */
		private void checkUrlFormat(String baseUrl) {
			UrlValidator urlValidator = getInstance();
			if (!baseUrl.isEmpty() && !urlValidator.isValid(baseUrl)) {
				throw new InvalidGitHubConfigurationException("Field 'base-url' doesn't conform to url format.\nPlease check your .gitconfig.yml");
			}
		}

		/**
		 * Check gitHub configuration has unsupported fields.
		 *
		 * @throws InvalidGitHubConfigurationException occurs when unsupported field is exist.
		 * */
		private void checkUnsupportedField(JsonNode jsonNode) {
			Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
			while (fields.hasNext()) {
				Entry<String, JsonNode> field = fields.next();
				String key = field.getKey();
				if (!isSupportedField(key)) {
					throw new InvalidGitHubConfigurationException("Field '" + key + "' isn't supported.\n" +
						"Please check your .gitconfig.yml");
				}
			}
		}
	}
}
