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
 * Category of {@link Status}. This class provides the characteristic of each status.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum StatusCategory {
	/**
	 * Ready to run..
	 */
	PREPARE("blue.png", false, true, false),
	/**
	 * Processing.
	 */
	PROGRESSING("blue_anime.gif", true, false, false),
	/**
	 * Testing..
	 */
	TESTING("green_anime.gif", true, false, false),
	/**
	 * Finished normally.
	 */
	FINISHED("green.png", false, true, true),
	/**
	 * Stopped by error .
	 */
	ERROR("red.png", false, true, true),
	/**
	 * Stopped by user.
	 */
	STOP("grey.png", false, true, true);

	private final boolean stoppable;
	private final boolean deletable;
	private final boolean reportable;
	private final String iconName; 

	StatusCategory(String iconName, boolean stoppable, boolean deletable, boolean reportable) {
		this.iconName = iconName;
		this.stoppable = stoppable;
		this.deletable = deletable;
		this.reportable = reportable;
	}

	public boolean isStoppable() {
		return stoppable;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public String getIconName() {
		return iconName;
	}

	public boolean isReportable() {
		return reportable;
	}
}
