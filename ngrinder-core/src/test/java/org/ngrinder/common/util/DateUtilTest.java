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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class DateUtilTest {

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#dateToString(java.util.Date)}.
	 */
	@Test
	public void testDateToString() {
		String dateStr = DateUtil.dateToString(new Date());
		assertThat(dateStr, notNullValue());
	}

	@Test
	public void testGetFilteredTimeZoneMap() {
		Map<String, String> tzMap = DateUtil.getFilteredTimeZoneMap();
		assertThat(tzMap, notNullValue());
		tzMap = DateUtil.getFilteredTimeZoneMap();
		assertThat(tzMap, notNullValue());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#toSimpleDate(java.lang.String)}.
	 * @throws ParseException 
	 */
	@Test
	public void testToSimpleDate() throws ParseException {
		String dateStr = DateUtil.dateToString(new Date());
		Date newDate = DateUtil.toSimpleDate(dateStr);
		assertThat(newDate, notNullValue());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#toDate(java.lang.String)}.
	 * @throws ParseException 
	 */
	@Test
	public void testToDate() throws ParseException {
		String dateStr = DateUtil.dateToString(new Date());
		Date newDate = DateUtil.toDate(dateStr);
		assertThat(newDate, notNullValue());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#addDay(java.util.Date, int)}.
	 */
	@Test
	public void testAddDay() {
		Date newDate = DateUtil.addDay(new Date(), 5);
		assertThat(newDate, notNullValue());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#ms2Time(long)}.
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
		long expected = 1000L * ((((day * 24) + hour) * 60 + min) * 60 + second );
		assertThat(duration, is(expected));
		

		day = 30;
		hour = 10;
		min = 10;
		second = 10;
		
		duration = DateUtil.timeToMs(day, hour, min, second);
		expected = 1000L * ((((day * 24) + hour) * 60 + min) * 60 + second );
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
