package org.ngrinder.script.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.util.Arrays.stream;

@Getter
@AllArgsConstructor
public enum GitHubConfigField {
	NAME("name"), OWNER("owner"), REPO("repo"), ACCESS_TOKEN("access-token"),
	BRANCH("branch"), BASE_URL("base-url"), REVISION("revision"), SCRIPT_ROOT("script-root");

	private String value;

	public static boolean isSupportedField(String fieldName) {
		return stream(values()).anyMatch(field -> field.getValue().equals(fieldName));
	}
}
