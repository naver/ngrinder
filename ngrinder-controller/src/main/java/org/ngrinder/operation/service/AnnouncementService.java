package org.ngrinder.operation.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Announcement operating service
 * 
 * @author AlexQin
 * @since 3.1
 */
@Service
public class AnnouncementService {

	private static final Logger LOG = LoggerFactory.getLogger(AnnouncementService.class);

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
	 * 			file content.
	 * @return save successfully or not.
	 */
	public boolean saveAnnouncement(String content) {
		File sysFile = config.getHome().getSubFile("announcement.conf");

		try {
			FileUtils.writeStringToFile(sysFile, content);
		} catch (IOException e) {
			LOG.error("Error while writing announcement.conf file.");
			return false;
		}
		config.loadAnnouncement(); //refresh the content of announcement.
		
		return true;
	}
}
