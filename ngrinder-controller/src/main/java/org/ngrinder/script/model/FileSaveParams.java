package org.ngrinder.script.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileSaveParams {
	private FileEntry fileEntry;
	private String targetHosts;
	private String validated = "0";
	private boolean createLibAndResource;
}
