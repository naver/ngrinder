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
package org.ngrinder.security;

/**
 * DataHolder for user language and timezone.
 * 
 * @author JunHo Yoon
 * @since 3.0.2
 */
public class LanguageAndTimezone {
	private String language;
	private String timezone;

	/**
	 * Constructor.
	 * 
	 * @param locale
	 *            locale string
	 * @param timezone
	 *            timezone string
	 */
	public LanguageAndTimezone(String locale, String timezone) {
		this.setLanguage(locale);
		this.timezone = timezone;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
