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
package org.ngrinder.home.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.home.model.PanelEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Data retriever for index page display.
 * 
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Component
public class HomeService {
	private static final Logger LOG = LoggerFactory.getLogger(HomeService.class);

	private Map<String, String> openIssueQuery = new HashMap<String, String>(1);
	private Map<String, String> closeIssueQuery = new HashMap<String, String>(1);

	@PostConstruct
	public void init() {
		openIssueQuery.put("state", "open");
		openIssueQuery.put("labels", "announcement");
		closeIssueQuery.put("state", "close");
		closeIssueQuery.put("labels", "announcement");
	}

	@SuppressWarnings("unchecked")
	@Cacheable(value = "left_panel_entries")
	public List<PanelEntry> getLeftPanelEntries() {
		SyndFeedInput input = new SyndFeedInput();
		XmlReader reader = null;
		try {
			List<PanelEntry> panelEntries = new ArrayList<PanelEntry>();
			reader = new XmlReader(new URL(NGrinderConstants.NGRINDER_NEWS_RSS_URL));
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
			List<Issue> issues = service.getIssues(repo, openIssueQuery);
			issues = issues.size() >= 8 ? issues.subList(0, 7) : issues;
			for (Issue each : issues) {
				PanelEntry entry = new PanelEntry();
				entry.setAuthor(each.getUser().getName());
				entry.setLastUpdatedDate(each.getUpdatedAt() == null ? each.getCreatedAt() : each.getUpdatedAt());
				entry.setTitle(each.getTitle());
				entry.setLink(each.getHtmlUrl());
				panelEntries.add(entry);
			}
			issues = service.getIssues(repo, closeIssueQuery);
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
