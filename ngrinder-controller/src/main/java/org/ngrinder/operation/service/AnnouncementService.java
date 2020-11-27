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
package org.ngrinder.operation.service;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Announcement operating service.
 *
 * @since 3.1
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementService.class);

	private final Config config;

	/**
	 * Get announcement.conf file content.
	 *
	 * @return file content.
	 */
	public String getOne() {
		return config.getAnnouncement();
	}

	/**
	 * Check the announcement was changed since 1 week ago.
	 *
	 * @return true if it's new one
	 */
	public boolean isNew() {
		Date announcementDate = config.getAnnouncementDate();
		if (announcementDate != null) {
			Date weekAgo = DateUtils.addDays(new Date(), -7);
			return announcementDate.after(weekAgo);
		} else {
			return false;
		}
	}

	/**
	 * Save content to announcement.conf file.
	 *
	 * @param content file content.
	 */
	public void save(String content) {
		try {
			FileUtils.writeStringToFile(config.getHome().getSubFile("announcement.conf"), content, "UTF-8");
			config.loadAnnouncement();
		} catch (IOException e) {
			LOGGER.error("Error while writing announcement file.");
			throw new NGrinderRuntimeException("Error while writing announcement file.");
		}
	}
}
