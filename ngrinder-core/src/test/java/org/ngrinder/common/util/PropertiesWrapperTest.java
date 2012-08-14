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
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class PropertiesWrapperTest {

	/**
	 * Test method for {@link org.ngrinder.common.util.PropertiesWrapper#PropertiesWrapper(java.util.Properties)}.
	 */
	@Test
	public void testPropertiesWrapper() {
		Properties prop = new Properties();
		prop.put("key1", "1");
		prop.put("key2", "value2");
		PropertiesWrapper propWrapper = new PropertiesWrapper(prop);
		
		propWrapper.addProperty("key3", "3");
		propWrapper.addProperty("key4", "value4");
		
		int value1 = propWrapper.getPropertyInt("key1", 0);
		assertThat(value1, is(1));
		int value3 = propWrapper.getPropertyInt("key3", 0);
		assertThat(value3, is(3));
		int noValue = propWrapper.getPropertyInt("NoValueKey", 0);
		assertThat(noValue, is(0));
		
		String value2 = propWrapper.getProperty("key2", "null");
		assertThat(value2, is("value2"));
		String value4 = propWrapper.getProperty("key4", "null");
		assertThat(value4, is("value4"));
		String nullValueStr = propWrapper.getProperty("NoValueKey", "null");
		assertThat(nullValueStr, is("null"));
		
		String newValue4 = propWrapper.getProperty("key4", "No value found for:{}");
		assertThat(newValue4, is("value4"));
		nullValueStr = propWrapper.getProperty("NoValueKey", "null", "No value found for:{}");
		assertThat(nullValueStr, is("null"));
	
	}
}
