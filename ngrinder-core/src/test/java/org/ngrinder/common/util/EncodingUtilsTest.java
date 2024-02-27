package org.ngrinder.common.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EncodingUtilsTest {
	@Test
	public void testPathEncoding() {
		assertThat(EncodingUtils.encodePathWithUTF8("hello"), is("hello"));
		assertThat(EncodingUtils.encodePathWithUTF8("한국"), is("%ED%95%9C%EA%B5%AD"));
		assertThat(EncodingUtils.encodePathWithUTF8("hello/한국"), is("hello/%ED%95%9C%EA%B5%AD"));
		assertThat(EncodingUtils.encodePathWithUTF8("hello/한국/와우"), is("hello/%ED%95%9C%EA%B5%AD/%EC%99%80%EC%9A%B0"));
		assertThat(EncodingUtils.encodePathWithUTF8("--hello/한국/와우/"), is("--hello/%ED%95%9C%EA%B5%AD/%EC%99%80%EC%9A%B0/"));
		assertThat(EncodingUtils.encodePathWithUTF8("/hello/한국/와우/"), is("/hello/%ED%95%9C%EA%B5%AD/%EC%99%80%EC%9A%B0/"));
		assertThat(EncodingUtils.encodePathWithUTF8("/hello/한국.-/와우/"), is("/hello/%ED%95%9C%EA%B5%AD.-/%EC%99%80%EC%9A%B0/"));
	}
}
