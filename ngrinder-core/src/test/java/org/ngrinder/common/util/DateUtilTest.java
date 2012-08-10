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

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#getFilteredTimeZoneMap()}.
	 */
	@Test
	public void testGetFilteredTimeZoneMap() {
		Map<String, String> tzMap = DateUtil.getFilteredTimeZoneMap();
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
	 * Test method for {@link org.ngrinder.common.util.DateUtil#formatDate(java.util.Date, java.lang.String)}.
	 */
	@Test
	public void testFormatDate() {
		String dateStr = DateUtil.formatDate(new Date(), "MM-dd-yyyy");
		assertThat(dateStr, notNullValue());
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
		String durationStr = DateUtil.ms2Time(new Date().getTime());
		assertThat(durationStr, notNullValue());
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.DateUtil#timeToMs(int, int, int, int)}.
	 */
	@Test
	public void testTimeToMs() {
		int day = 30;
		int hour = 1;
		int min = 1;
		int second = 1;
		
		long duration = DateUtil.timeToMs(day, hour, min, second);
		long expected = 1000L * ((((day * 24) + hour) * 60 + min) * 60 + second );
		assertThat(duration, is(expected));
	}

}
