package org.ngrinder.home.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.ngrinder.home.model.PanelEntry;
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
	@Cacheable(value = "left_panel_entries")
	public List<PanelEntry> getLeftPanelEntries() {
		SyndFeedInput input = new SyndFeedInput();
		XmlReader reader = null;
		try {
			List<PanelEntry> panelEntries = new ArrayList<PanelEntry>();
			reader = new XmlReader(new URL("http://www.cubrid.org/wiki_ngrinder/rss"));
			SyndFeed feed = input.build(reader);
			List<SyndEntryImpl> entries = (List<SyndEntryImpl>) (feed.getEntries().size() >= 8 ? feed.getEntries()
					.subList(0, 7) : feed.getEntries());
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
			LOG.error("Error while patching ngriner rss", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return Collections.emptyList();
	}

	@Cacheable(value = "right_panel_entries")
	public List<PanelEntry> getRightPanelEntries() {

		IssueService service = new IssueService();
		RepositoryId repo = new RepositoryId("nhnopensource", "ngrinder");
		try {

			List<PanelEntry> panelEntries = new ArrayList<PanelEntry>();
			List<Issue> issues = service.getIssues(repo, new HashMap<String, String>() {
				{
					put("state", "open");
				}
			});
			issues = issues.size() >= 8 ? issues.subList(0, 7) : issues;
			for (Issue each : issues) {
				PanelEntry entry = new PanelEntry();
				entry.setAuthor(each.getUser().getName());
				entry.setLastUpdatedDate(each.getUpdatedAt() == null ? each.getCreatedAt() : each.getUpdatedAt());
				entry.setTitle(each.getTitle());
				entry.setLink(each.getHtmlUrl());
				panelEntries.add(entry);
			}

			issues = service.getIssues(repo, new HashMap<String, String>() {
				{
					put("state", "close");
				}
			});

			for (Issue each : issues) {
				PanelEntry entry = new PanelEntry();
				entry.setAuthor(each.getUser().getName());
				entry.setLastUpdatedDate(each.getUpdatedAt() == null ? each.getCreatedAt() : each.getUpdatedAt());
				entry.setTitle(each.getTitle());
				entry.setLink(each.getHtmlUrl());
				panelEntries.add(entry);
			}

			Collections.sort(panelEntries);
			return panelEntries;
		} catch (Exception e) {
			LOG.error("Error while patching ngriner rss", e);
		}
		return Collections.emptyList();
	}
}
