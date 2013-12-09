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
package org.ngrinder.service;

import org.ngrinder.model.IFileEntry;
import org.ngrinder.model.User;

/**
 * Interface of ScriptValidationService. This interface can be injected in the plugin
 *
 * @author JunHo Yoon
 * @since 3.1.3
 */
public interface IScriptValidationService {

	/**
	 * Validate Script.
	 *
	 * It's quite complex.. to validate script, we need to write jar files and script. Furthermore, to
	 * make a small log.. We have to copy optimized logback_worker.xml
	 *
	 * Finally this method returns the path of validating result file.
	 *
	 * @param user           user
	 * @param scriptEntry    scriptEntity.. at least path should be provided.
	 * @param useScriptInSVN true if the script content in SVN is used. otherwise, false
	 * @param hostString     HOSTNAME:IP,... pairs for host manipulation
	 * @return validation result.
	 */
	public abstract String validate(User user, IFileEntry scriptEntry, boolean useScriptInSVN, String hostString);
}