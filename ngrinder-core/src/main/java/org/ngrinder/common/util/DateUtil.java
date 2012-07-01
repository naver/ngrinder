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
package org.ngrinder.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DateUtil {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	private static int ss = 1000;
	private static int mi = ss * 60;
	private static int hh = mi * 60;
	private static int dd = hh * 24;

	private static Map<String, String> timezoneIDMap;

	public static String dateToString(Date date) {
		return dateFormat.format(date);
	}

	public static Map<String, String> getFilteredTimeZoneMap() {
		if (timezoneIDMap == null) {
			timezoneIDMap = new LinkedHashMap<String, String>();
			String[] ids = TimeZone.getAvailableIDs();
			for (String id : ids) {
				TimeZone zone = TimeZone.getTimeZone(id);
				int offset = zone.getRawOffset();
				int offsetSecond = offset / 1000;
				int hour = offsetSecond / 3600;
				int minutes = (offsetSecond % 3600) / 60;
				timezoneIDMap.put(id, String.format("(GMT%+d:%02d) %s", hour, minutes, id));
			}
		}
		return timezoneIDMap;
	}

	public static Date toSimpleDate(String strDate) throws ParseException {
		return simpleDateFormat.parse(strDate);
	}

	public static Date toDate(String strDate) throws ParseException {
		return dateFormat.parse(strDate);
	}

	/**
	 * get start day of a given date. Because the default start day of Calendar
	 * is "Sunday", when the given date is "Sunday", we need to add "-7" to date
	 * field to get previous week.
	 * 
	 * @param date
	 * @return
	 */
	public static Calendar getWeekStart(Calendar date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTimeInMillis());
		// Calendar.MONDAY is 2
		boolean isSunday = false;
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			isSunday = true;
		}
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (isSunday) {
			calendar.add(Calendar.DAY_OF_WEEK, -7);
		}
		return calendar;

	}

	public static String formatDate(Date date, String pattern) {
		if (date == null)
			throw new IllegalArgumentException("date is null");
		if (pattern == null)
			throw new IllegalArgumentException("pattern is null");

		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}

	public static Date addDay(Date date, int days) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}

	public static String ms2Time(long ms) {

		long day = ms / dd;
		long hour = (ms - day * dd) / hh;
		long minute = (ms - day * dd - hour * hh) / mi;
		long second = (ms - day * dd - hour * hh - minute * mi) / ss;
		// long milliSecond = ms - day * dd - hour * hh - minute * mi - second *
		// ss;

		String strDay = day < 10 ? "0" + day : "" + day;
		String strHour = hour < 10 ? "0" + hour : "" + hour;
		String strMinute = minute < 10 ? "0" + minute : "" + minute;
		String strSecond = second < 10 ? "0" + second : "" + second;
		// String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" +
		// milliSecond;
		// strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" +
		// strMilliSecond;
		return strDay + ":" + strHour + ":" + strMinute + ":" + strSecond;
	}

	public static int timeToMs(int day, int hour, int min, int sec) {
		return (((day * 24 + hour) * 60 + min) * 60 + sec) * 1000;
	}

}
