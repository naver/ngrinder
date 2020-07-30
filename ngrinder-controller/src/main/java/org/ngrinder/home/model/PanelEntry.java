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
package org.ngrinder.home.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.ngrinder.common.util.DateUtils;

/**
 * Panel entry which will be shown in main page.
 *
 * @since 3.0
 */
@Getter
@Setter
public class PanelEntry implements Comparable<PanelEntry> {
	private static final int NEW_LIMIT = 5;

	private String title;
	private Date lastUpdatedDate;
	private String link;
	private String author;

	public boolean isNew() {
		return DateUtils.addDay(lastUpdatedDate, NEW_LIMIT).compareTo(new Date()) > 0;
	}

	@Override
	public int compareTo(PanelEntry o) {
		return o.lastUpdatedDate.compareTo(lastUpdatedDate);
	}
}
