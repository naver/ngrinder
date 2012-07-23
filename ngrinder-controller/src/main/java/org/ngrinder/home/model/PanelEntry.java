package org.ngrinder.home.model;

import java.util.Date;

import org.ngrinder.common.util.DateUtil;

/**
 * Panel entry which will be shown in main page.
 * 
 * @author JunHo Yoon
 */
public class PanelEntry implements Comparable<PanelEntry> {
	private String title;
	private Date lastUpdatedDate;
	private String link;
	private String author;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isNew() {
		return DateUtil.addDay(lastUpdatedDate, 5).compareTo(new Date()) > 0;
	}

	@Override
	public int compareTo(PanelEntry o) {
		return o.lastUpdatedDate.compareTo(lastUpdatedDate);
	}
}
