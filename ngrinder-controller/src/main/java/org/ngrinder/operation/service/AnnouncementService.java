/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.operation.service;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Announcement operating service.
 * 
 * @author AlexQin
 * @since 3.1
 */
@Service
public class AnnouncementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementService.class);

	@Autowired
	private Config config;

	/**
	 * Get announcement.conf file content.
	 * 
	 * @return file content.
	 */
	public String getAnnouncement() {
		return config.getAnnouncement();
	}

	/**
	 * Save content to announcement.conf file.
	 * 
	 * @param content
	 *            file content.
	 * @return save successfully or not.
	 */
	public boolean saveAnnouncement(String content) {
		try {
			FileUtils.writeStringToFile(config.getHome().getSubFile("announcement.conf"), content);
		} catch (IOException e) {
			LOGGER.error("Error while writing announcement file.");
			return false;
		}
		return true;
	}
}
