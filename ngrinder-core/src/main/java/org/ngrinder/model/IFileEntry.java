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
package org.ngrinder.model;


/**
 * File Entry model interface.
 *
 * It's little bit out of convention. However I separate this interface on model because of backward
 * compatibility.
 * 
 * @author JunHo Yoon
 */
public interface IFileEntry {

	/**
	 * Get the relative path of file.
	 * 
	 * @return relative path
	 */
	public abstract String getPath();

	/**
	 * Get the content.
	 * 
	 * @return content string
	 */
	public abstract String getContent();

	/**
	 * Get the encoding of content.
	 * 
	 * @return encoding
	 */
	public abstract String getEncoding();

	/**
	 * Get the create user.
	 * 
	 * @return user
	 * @since 3.2
	 */
	@SuppressWarnings("UnusedDeclaration")
	public abstract User getCreatedUser();

	/**
	 * Get the revision of the file entity.
	 * 
	 * @return revision
	 * @since 3.2
	 */
	public abstract long getRevision();

}