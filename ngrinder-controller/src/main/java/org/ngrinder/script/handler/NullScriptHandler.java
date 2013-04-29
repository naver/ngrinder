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

import java.io.File;

import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

/**
 * Null {@link ScriptHandler} which implements Null object pattern.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
@Component
public class NullScriptHandler extends ScriptHandler {

	/**
	 * Constructor.
	 */
	public NullScriptHandler() {
		super("", "", null, "plain");
	}

	@Override
	public Integer order() {
		return 500;
	}

	@Override
	public void prepareDist(String identifier, User user, FileEntry script, //
					File distDir, PropertiesWrapper properties) {

	}

	@Override
	public boolean canHandle(FileEntry fileEntry) {
		return true;
	}

	@Override
	public String checkSyntaxErrors(String content) {
		return null;
	}
}