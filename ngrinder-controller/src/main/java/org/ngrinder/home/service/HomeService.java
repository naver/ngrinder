package org.ngrinder.home.service;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@Component
public class HomeService {
	private static final Logger LOG = LoggerFactory.getLogger(HomeService.class);

	@SuppressWarnings("unchecked")
	@Cacheable(value = "ngrinder_wiki_rss_list")
	public List<SyndEntryImpl> getRssEntries() {
		SyndFeedInput input = new SyndFeedInput();
		XmlReader reader = null;
		try {
			reader = new XmlReader(new URL("http://www.cubrid.org/wiki_ngrinder/rss"));
			SyndFeed feed = input.build(reader);
			return (List<SyndEntryImpl>) feed.getEntries();
		} catch (Exception e) {
			LOG.error("Error while patching ngriner rss", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return Collections.emptyList();

	}
}
