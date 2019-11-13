package org.ngrinder.script.model;

import lombok.*;

@Getter
@Setter
@ToString
public class GitConfig {
	private String name;
	private String accessToken;
	private String url;
}
