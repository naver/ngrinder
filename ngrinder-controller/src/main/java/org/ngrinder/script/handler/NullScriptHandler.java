/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.script.handler;

import static org.ngrinder.common.util.CollectionUtils.getValue;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;

import java.io.File;
import java.util.Map;

import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.springframework.stereotype.Component;

/**
 * Null {@link ScriptHandler} which implements Null object pattern.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
@Component
public class NullScriptHandler extends ScriptHandler {

	private Map<FileType, String> codeMirrorKey = newHashMap();

	/**
	 * Constructor.
	 */
	public NullScriptHandler() {
		super("", "", null, null);
		codeMirrorKey.put(FileType.PROPERTIES, "properties");
		codeMirrorKey.put(FileType.XML, "xml");
	}

	@Override
	public Integer order() {
		return 1000;
	}

	@Override
	public void prepareDist(Long testId, User user, FileEntry script, //
	                        File distDir, PropertiesWrapper properties, ProcessingResultPrintStream processingResult) {

	}

	@SuppressWarnings("SpellCheckingInspection")
	@Override
	public boolean isValidatable() {
		return false;
	}

	/**
	 * Alternative access to code mirror key.
	 *
	 * @param fileType file type
	 * @return appropriate code mirror key. if nothing, return shell
	 */
	@SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
	public String getCodemirrorKey(FileType fileType) {
		return getValue(codeMirrorKey, fileType, "shell");
	}

	@Override
	public boolean canHandle(FileEntry fileEntry) {
		return true;
	}

	@Override
	public Integer displayOrder() {
		return -1;
	}

	@Override
	public String checkSyntaxErrors(String path, String content) {
		// TODO Auto-generated method stub
		return null;
	}
}