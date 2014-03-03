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

import org.apache.commons.lang.StringUtils;

/**
 * Date Utility.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class DateUtils {

	private static final int CONSTANT_10 = 10;
	private static final int CONSTANT_24 = 24;
	private static final int CONSTANT_60 = 60;
	private static final int CONSTANT_1000 = 1000;
	// private static final int CONSTANT_MINUS_7 = -7;
	private static final int SS = CONSTANT_1000;
	private static final int MI = SS * CONSTANT_60;
	private static final int HH = MI * CONSTANT_60;
	private static final int DD = HH * CONSTANT_24;

	private static Map<String, String> timezoneIDMap;

	/**
	 * Get the time in long format : "yyyyMMddHHmmss".
	 * 
	 * @param date	date to be format
	 * @return time time in format of long type
	 */
	public static long getCollectTimeInLong(Date date) {
		SimpleDateFormat collectTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return Long.valueOf(collectTimeFormat.format(date));
	}

	/**
	 * Convert user date to new date with server side Locale.
	 * 
	 * @param userTimeZone	user TimeZone id
	 * @param userDate		date in user's Local
	 * @return serverDate data in server's Local
	 */
	public static Date convertToServerDate(String userTimeZone, Date userDate) {
		TimeZone userLocal = TimeZone.getTimeZone(userTimeZone);
		int rawOffset = TimeZone.getDefault().getRawOffset() - userLocal.getRawOffset();
		return new Date(userDate.getTime() + rawOffset);
	}

	/**
	 * Convert server date to new date with user Locale.
	 * 
	 * @param userTimeZone	user TimeZone id
	 * @param serverDate	date in server's Local
	 * @return serverDate data in user's Local
	 */
	public static Date convertToUserDate(String userTimeZone, Date serverDate) {
		TimeZone userLocal = TimeZone.getTimeZone(userTimeZone);
		int rawOffset = userLocal.getRawOffset() - TimeZone.getDefault().getRawOffset();
		return new Date(serverDate.getTime() + rawOffset);
	}

	/**
	 * Format date to {@value #FULL_DATE_FORMAT}.
	 * 
	 * @param date	date
	 * @return formatted string
	 */
	public static String dateToString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
	}

	/**
	 * Get time zones.
	 * 
	 * @return map time zone id and GMT
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
				timezoneIDMap.put(TimeZone.getTimeZone(id).getDisplayName(),
								String.format("(GMT%+d:%02d) %s", hour, minutes, id));
			}
		}
		return timezoneIDMap;
	}

	/**
	 * Convert string date to Date with {@value #SIMPLE_DATE_FORMAT}.
	 * 
	 * @param strDate	date string
	 * @return date
	 * @throws ParseException
	 *             thrown when the given strDate is not {@link #SIMPLE_DATE_FORMAT}
	 */
	public static Date toSimpleDate(String strDate) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		return simpleDateFormat.parse(strDate);
	}

	/**
	 * Convert string date to Date with {@value #FULL_DATE_FORMAT}.
	 * 
	 * @param strDate
	 *            date string
	 * @return date
	 * 
	 * @throws ParseException
	 *             thrown when the given strDate is not {@link #FULL_DATE_FORMAT}
	 */
	public static Date toDate(String strDate) throws ParseException {
		SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return fullDateFormat.parse(strDate);
	}

	/**
	 * Add days on date.
	 * 
	 * @param date	base date
	 * @param days	days to be added.
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
	 * @param ms	Millisecond
	 * @return DD:HH:MM:SS formatted string
	 */
	public static String ms2Time(long ms) {
		long day = ms / DD;
		long hour = (ms - day * DD) / HH;
		long minute = (ms - day * DD - hour * HH) / MI;
		long second = (ms - day * DD - hour * HH - minute * MI) / SS;

		String strDay = day < CONSTANT_10 ? "0" + day : "" + day;
		String strHour = hour < CONSTANT_10 ? "0" + hour : "" + hour;
		String strMinute = minute < CONSTANT_10 ? "0" + minute : "" + minute;
		String strSecond = second < CONSTANT_10 ? "0" + second : "" + second;
		strDay = (StringUtils.equals(strDay, "00")) ? "" : strDay + ":";
		return strDay + strHour + ":" + strMinute + ":" + strSecond;
	}

	/**
	 * Convert time to millisecond.
	 * 
	 * @param day	day
	 * @param hour	hour
	 * @param min	min
	 * @param sec	sec
	 * @return converted millisecond
	 */
	public static long timeToMs(int day, int hour, int min, int sec) {
		return ((long) CONSTANT_1000) * (((day * CONSTANT_24 + hour) * CONSTANT_60 + min) * CONSTANT_60 + sec);
	}

	/**
	 * Compare two date in minute detail.
	 * 
	 * @param d1	date
	 * @param d2	date
	 * @return true if two {@link Date} are same in minute level
	 */
	public static boolean compareDateEndWithMinute(Date d1, Date d2) {
		SimpleDateFormat dateFormatEndWithMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String s1 = dateFormatEndWithMinute.format(d1);
		String s2 = dateFormatEndWithMinute.format(d2);
		return (s1.equals(s2));
	}

}
