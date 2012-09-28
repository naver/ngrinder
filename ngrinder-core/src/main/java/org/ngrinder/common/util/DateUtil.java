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

import org.apache.commons.lang.StringUtils;

/**
 * Date Utility.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class DateUtil { 

	private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat dateFormatEndWithMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final SimpleDateFormat collectTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

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
	 * get the time in long format : "yyyyMMddHHmmss".
	 * @param date
	 * 			  date to be format
	 * @return time
	 * 			  time in format of long type
	 */
	public static long getCollectTimeInLong(Date date) {
		return Long.valueOf(collectTimeFormat.format(date));
	}
	
	/**
	 * convert user date to new date with server side Locale.
	 * @param userTimeZone
	 * 			  user TimeZone id
	 * @param userDate
	 * 			  date in user's Local
	 * @return serverDate
	 * 			  data in server's Local
	 */
	public static Date convertToServerDate(String userTimeZone, Date userDate) {
		TimeZone userLocal = TimeZone.getTimeZone(userTimeZone);
		int rawOffset = TimeZone.getDefault().getRawOffset() - userLocal.getRawOffset();
		return new Date(userDate.getTime() + rawOffset);
	}
	
	/**
	 * convert server date to new date with user Locale.
	 * @param userTimeZone
	 * 			  user TimeZone id
	 * @param serverDate
	 * 			  date in server's Local
	 * @return serverDate
	 * 			  data in user's Local
	 */
	public static Date convertToUserDate(String userTimeZone, Date serverDate) {
		TimeZone userLocal = TimeZone.getTimeZone(userTimeZone);
		int rawOffset = userLocal.getRawOffset() - TimeZone.getDefault().getRawOffset();
		return new Date(serverDate.getTime() + rawOffset);
	}

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
				timezoneIDMap.put(TimeZone.getTimeZone(id).getDisplayName(),
						String.format("(GMT%+d:%02d) %s", hour, minutes, id));
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
	public static long timeToMs(int day, int hour, int min, int sec) {
		return ((long) CONSTANT_1000) * (((day * CONSTANT_24 + hour) * CONSTANT_60 + min) * CONSTANT_60 + sec);
	}

	public static boolean compareDateEndWithMinute(Date d1, Date d2) {
		String s1 = dateFormatEndWithMinute.format(d1);
		String s2 = dateFormatEndWithMinute.format(d2);
		if (s1.equals(s2))
			return true;
		else
			return false;
	}

}
