package org.ngrinder.tracking;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Focus point of the application. It can represent data points like application
 * load, application module load, user actions, error events etc.
 * 
 * @author : Siddique Hameed
 * @version : 0.1
 */

public class FocusPoint {

	private String name;
	private FocusPoint parentFocusPoint;
	private static final String URI_SEPARATOR = "/";
	private static final String TITLE_SEPARATOR = "-";

	public FocusPoint(String name) {
		this.name = name;
	}

	public FocusPoint(String name, FocusPoint parentFocusPoint) {
		this(name);
		this.parentFocusPoint = parentFocusPoint;
	}

	public String getName() {
		return name;
	}

	public void setParentTrackPoint(FocusPoint parentFocusPoint) {
		this.parentFocusPoint = parentFocusPoint;
	}

	public FocusPoint getParentFocusPoint() {
		return parentFocusPoint;
	}

	public String getContentURI() {
		StringBuffer contentURIBuffer = new StringBuffer();
		getContentURI(contentURIBuffer, this);
		return contentURIBuffer.toString();
	}

	public String getContentTitle() {
		StringBuffer titleBuffer = new StringBuffer();
		getContentTitle(titleBuffer, this);
		return titleBuffer.toString();
	}

	private void getContentURI(StringBuffer contentURIBuffer, FocusPoint focusPoint) {
		FocusPoint parentFocuPoint = focusPoint.getParentFocusPoint();

		if (parentFocuPoint != null) {
			getContentURI(contentURIBuffer, parentFocuPoint);
		}
		contentURIBuffer.append(URI_SEPARATOR);
		contentURIBuffer.append(encode(focusPoint.getName()));
	}

	private String encode(String name) {
		try {
			return URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return name;
		}
	}

	private void getContentTitle(StringBuffer titleBuffer, FocusPoint focusPoint) {
		FocusPoint parentFocusPoint = focusPoint.getParentFocusPoint();

		if (parentFocusPoint != null) {
			getContentTitle(titleBuffer, parentFocusPoint);
			titleBuffer.append(TITLE_SEPARATOR);
		}
		titleBuffer.append(encode(focusPoint.getName()));
	}
}
