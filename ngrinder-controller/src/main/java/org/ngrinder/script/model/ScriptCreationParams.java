package org.ngrinder.script.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScriptCreationParams {
	private String FileName;
	private String testUrl;
	private String options;
	private String scriptType;
	private boolean createLibAndResource;
}
