package org.ngrinder.script.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScriptValidationParams {
	private FileEntry fileEntry;
	private String hostString;
}
