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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class DateUtilTest {

	/**
	 * Test method for {@link DateUtil#dateToString(java.util.Date)}.
	 */
	@Test
	public void testDateToString() {
		String dateStr = DateUtil.dateToString(new Date());
		assertThat(dateStr, notNullValue());
	}

	@Test
	public void testConvertToServerDate() {
		String userLocaleId = "Asia/Seoul";
		TimeZone tz = TimeZone.getTimeZone(userLocaleId);

		Date userDate = new Date();
		Date serverDate = DateUtil.convertToServerDate(userLocaleId, userDate);
		// userDate - serverDate should be equal as offset
		assertThat((int) (userDate.getTime() - serverDate.getTime()), is(tz.getRawOffset()
						- TimeZone.getDefault().getRawOffset()));
	}

	@Test
	public void testConvertToServerDateSameLocal() {
		String userLocaleId = "Asia/Shanghai";
		TimeZone tz = TimeZone.getTimeZone(userLocaleId);

		Date userDate = new Date();
		Date serverDate = DateUtil.convertToServerDate(userLocaleId, userDate);
		// userDate - serverDate should be equal as offset
		assertThat((int) (userDate.getTime() - serverDate.getTime()), is(tz.getRawOffset()
						- TimeZone.getDefault().getRawOffset()));
	}

	@Test
	public void testConvertToUserDate() {
		String userLocaleId = "Asia/Seoul";
		TimeZone tz = TimeZone.getTimeZone(userLocaleId);
		Date serverDate = new Date();
		Date userDate = DateUtil.convertToUserDate(userLocaleId, serverDate);
		assertThat((int) (userDate.getTime() - serverDate.getTime()), is(tz.getRawOffset()
						- TimeZone.getDefault().getRawOffset()));

		// convert the server date back to test.
		Date newServerDate = DateUtil.convertToServerDate(userLocaleId, userDate);
		assertThat(serverDate.getTime(), is(newServerDate.getTime()));
	}

	@Test
	public void testGetFilteredTimeZoneMap() {
		Map<String, String> tzMap = DateUtil.getFilteredTimeZoneMap();
		assertThat(tzMap, notNullValue());
		tzMap = DateUtil.getFilteredTimeZoneMap();
		assertThat(tzMap, notNullValue());
	}

	/**
	 * Test method for {@link DateUtil#toSimpleDate(java.lang.String)}.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testToSimpleDate() throws ParseException {
		String dateStr = DateUtil.dateToString(new Date());
		Date newDate = DateUtil.toSimpleDate(dateStr);
		assertThat(newDate, notNullValue());
	}

	/**
	 * Test method for {@link DateUtil#toDate(java.lang.String)}.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testToDate() throws ParseException {
		String dateStr = DateUtil.dateToString(new Date());
		Date newDate = DateUtil.toDate(dateStr);
		assertThat(newDate, notNullValue());
	}

	/**
	 * Test method for {@link DateUtil#addDay(java.util.Date, int)}.
	 */
	@Test
	public void testAddDay() {
		Date newDate = DateUtil.addDay(new Date(), 5);
		assertThat(newDate, notNullValue());
	}

	/**
	 * Test method for {@link DateUtil#ms2Time(long)}.
	 */
	@Test
	public void testMs2Time() {
		final long secTime = 1000;
		long timeMs = 10 * secTime;
		String durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("00:00:10"));

		timeMs = 10 * secTime + 33;
		durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("00:00:10"));

		timeMs = 10 * secTime;
		durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("00:00:10"));

		timeMs = 10 * secTime + 3 * 60 * secTime;
		durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("00:03:10"));

		timeMs = timeMs + 10 * 60 * secTime;
		durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("00:13:10"));

		timeMs = timeMs + 3 * 60 * 60 * secTime;
		durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("03:13:10"));

		timeMs = timeMs + 10 * 60 * 60 * secTime;
		durationStr = DateUtil.ms2Time(timeMs);
		assertThat(durationStr, is("13:13:10"));
	}

	@Test
	public void testTimeToMs() {
		int day = 3;
		int hour = 1;
		int min = 1;
		int second = 1;

		long duration = DateUtil.timeToMs(day, hour, min, second);
		long expected = 1000L * ((((day * 24) + hour) * 60 + min) * 60 + second);
		assertThat(duration, is(expected));

		day = 30;
		hour = 10;
		min = 10;
		second = 10;

		duration = DateUtil.timeToMs(day, hour, min, second);
		expected = 1000L * ((((day * 24) + hour) * 60 + min) * 60 + second);
		assertThat(duration, is(expected));

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testCompareDateEndWithMinute() {
		Date date1 = new Date();
		date1.setSeconds(0);

		Date date2 = new Date(date1.getTime());
		date2.setSeconds(10);
		assertTrue(DateUtil.compareDateEndWithMinute(date1, date2));
		assertTrue(DateUtil.compareDateEndWithMinute(date1, date1));

		date2 = new Date(date1.getTime());
		if (date1.getMinutes() > 1) {
			date2.setMinutes(date1.getMinutes() - 1);
		} else {
			date2.setMinutes(date1.getMinutes() + 1);
		}
		assertTrue(!DateUtil.compareDateEndWithMinute(date1, date2));

	}

}
