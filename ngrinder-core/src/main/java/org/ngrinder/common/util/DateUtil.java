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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Date Utility.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class DateUtil {

	private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.getDefault());
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	private static final int CONSTANT_10 = 10;
	private static final int CONSTANT_24 = 24;
	private static final int CONSTANT_60 = 60;
	private static final int CONSTANT_1000 = 1000;
	private static final int CONSTANT_MINUS_7 = -7;
	private static final int SS = CONSTANT_1000;
	private static final int MI = SS * CONSTANT_60;
	private static final int HH = MI * CONSTANT_60;
	private static final int DD = HH * CONSTANT_24;

	private static Map<String, String> timezoneIDMap;

	/**
	 * Format date to {@value #FULL_DATE_FORMAT}.
	 * 
	 * @param date
	 *            date
	 * @return formated string
	 */
	public static String dateToString(Date date) {
		return FULL_DATE_FORMAT.format(date);
	}

	/**
	 * Get time zones.
	 * 
	 * @return map typezone id and GMT
	 */
	public static Map<String, String> getFilteredTimeZoneMap() {
		if (timezoneIDMap == null) {
			timezoneIDMap = new LinkedHashMap<String, String>();
			String[] ids = TimeZone.getAvailableIDs();
			for (String id : ids) {
				TimeZone zone = TimeZone.getTimeZone(id);
				int offset = zone.getRawOffset();
				int offsetSecond = offset / CONSTANT_1000;
				int hour = offsetSecond / (CONSTANT_60 * CONSTANT_60);
				int minutes = (offsetSecond % (CONSTANT_60 * CONSTANT_60)) / CONSTANT_60;
				timezoneIDMap.put(id, String.format("(GMT%+d:%02d) %s", hour, minutes, id));
			}
		}
		return timezoneIDMap;
	}

	/**
	 * Convert string date to Date with {@value #SIMPLE_DATE_FORMAT}.
	 * 
	 * @param strDate
	 *            date string
	 * @return date
	 * @throws ParseException
	 *             occurs when given steDate is not {@link #SIMPLE_DATE_FORMAT}
	 */
	public static Date toSimpleDate(String strDate) throws ParseException {
		return SIMPLE_DATE_FORMAT.parse(strDate);
	}

	/**
	 * Convert string date to Date with {@value #FULL_DATE_FORMAT}.
	 * 
	 * @param strDate
	 *            date string
	 * @return date
	 * 
	 * @throws ParseException
	 *             occurs when given steDate is not {@link #FULL_DATE_FORMAT}
	 */
	public static Date toDate(String strDate) throws ParseException {
		return FULL_DATE_FORMAT.parse(strDate);
	}

	/**
	 * get start day of a given date. Because the default start day of Calendar
	 * is "Sunday", when the given date is "Sunday", we need to add "-7" to date
	 * field to get previous week.
	 * 
	 * @param date
	 *            calendar
	 * @return week start
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
			calendar.add(Calendar.DAY_OF_WEEK, CONSTANT_MINUS_7);
		}
		return calendar;

	}

	/**
	 * Format date with given pattern.
	 * 
	 * @param date
	 *            date
	 * @param pattern
	 *            pattern
	 * @return formatted date string
	 */
	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(checkNotNull(pattern, "pattern is null"));
		return formatter.format(checkNotNull(date, "date is null"));
	}

	/**
	 * Add days on date.
	 * 
	 * @param date
	 *            base date
	 * @param days
	 *            days to be added.
	 * @return added Date
	 */
	public static Date addDay(Date date, int days) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}

	/**
	 * Convert millisecond to DD:HH:MM:SS style.
	 * 
	 * @param ms
	 *            Millisecond
	 * @return DD:HH:MM:SS formated string
	 */
	public static String ms2Time(long ms) {

		long day = ms / DD;
		long hour = (ms - day * DD) / HH;
		long minute = (ms - day * DD - hour * HH) / MI;
		long second = (ms - day * DD - hour * HH - minute * MI) / SS;
		// long milliSecond = ms - day * dd - hour * hh - minute * mi - second *
		// ss;

		String strDay = day < CONSTANT_10 ? "0" + day : "" + day;
		String strHour = hour < CONSTANT_10 ? "0" + hour : "" + hour;
		String strMinute = minute < CONSTANT_10 ? "0" + minute : "" + minute;
		String strSecond = second < CONSTANT_10 ? "0" + second : "" + second;
		// String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" +
		// milliSecond;
		// strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" +
		// strMilliSecond;
		return strDay + ":" + strHour + ":" + strMinute + ":" + strSecond;
	}

	/**
	 * Convert time to millisecond.
	 * 
	 * @param day
	 *            day
	 * @param hour
	 *            hour
	 * @param min
	 *            min
	 * @param sec
	 *            sec
	 * @return converted millisecond
	 */
	public static int timeToMs(int day, int hour, int min, int sec) {
		return (((day * CONSTANT_24 + hour) * CONSTANT_60 + min) * CONSTANT_60 + sec) * CONSTANT_1000;
	}

}
