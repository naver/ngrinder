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
package org.ngrinder.home.service;

import static org.ngrinder.common.util.TypeConvertUtil.cast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.home.model.PanelEntry;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * nGrinder index page data retrieval service.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Component
public class HomeService {
	private static final int PANEL_ENTRY_SIZE = 7;

	private static final Logger LOG = LoggerFactory.getLogger(HomeService.class);

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
	}

	@Autowired
	private Config config;

	/**
	 * Get the let panel entries. if not configured, ngrinder nabble contents
	 * will be returned as defaults.
	 * 
	 * @return the list of {@link PanelEntry}
	 */
	@SuppressWarnings("unchecked")
	@Cacheable(value = "left_panel_entries")
	public List<PanelEntry> getLeftPanelEntries() {
		SyndFeedInput input = new SyndFeedInput();
		XmlReader reader = null;
		try {
			List<PanelEntry> panelEntries = new ArrayList<PanelEntry>();
			URL url = new URL(config.getSystemProperties().getProperty(NGrinderConstants.NGRINDER_PROP_FRONT_PAGE_RSS,
					NGrinderConstants.NGRINDER_NEWS_RSS_URL));
			reader = new XmlReader(url);
			SyndFeed feed = input.build(reader);
			List<SyndEntryImpl> entries = (List<SyndEntryImpl>) (feed.getEntries().subList(0,
					Math.min(feed.getEntries().size(), PANEL_ENTRY_SIZE)));
			for (SyndEntryImpl each : entries) {
				PanelEntry entry = new PanelEntry();
				entry.setAuthor(each.getAuthor());
				entry.setLastUpdatedDate(each.getUpdatedDate() == null ? each.getPublishedDate() : each
						.getUpdatedDate());
				entry.setTitle(each.getTitle());
				entry.setLink(each.getLink());
				panelEntries.add(entry);
			}
			Collections.sort(panelEntries);
			return panelEntries;

		} catch (Exception e) {
			LOG.error("Error while patching the rss for the home's left panel.", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return Collections.emptyList();
	}

	/**
	 * Get the right panel entries containing the entries from the given RSS
	 * url.
	 * 
	 * @param rssURL
	 *            rss url message
	 * @return {@link PanelEntry} list
	 */
	@Cacheable(value = "right_panel_entries")
	public List<PanelEntry> getRightPanelEntries(String rssURL) {
		SyndFeedInput input = new SyndFeedInput();
		XmlReader reader = null;
		try {
			List<PanelEntry> panelEntries = new ArrayList<PanelEntry>();
			URL url = new URL(rssURL);
			reader = new XmlReader(url);
			SyndFeed feed = input.build(reader);
			int count = 0;
			for (Object eachObj : (feed.getEntries())) {
				SyndEntryImpl each = cast(eachObj);
				if (!StringUtils.startsWithIgnoreCase(each.getTitle(), "Re: ")) {
					if (count++ > PANEL_ENTRY_SIZE) {
						break;
					}
					PanelEntry entry = new PanelEntry();
					entry.setAuthor(each.getAuthor());
					entry.setLastUpdatedDate(each.getUpdatedDate() == null ? each.getPublishedDate() : each
							.getUpdatedDate());
					entry.setTitle(each.getTitle());
					entry.setLink(each.getLink());

					panelEntries.add(entry);
				}
			}
			Collections.sort(panelEntries);
			return panelEntries;

		} catch (Exception e) {
			LOG.error("Error while patching the rss for the home's right panel.", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return Collections.emptyList();
	}
}
