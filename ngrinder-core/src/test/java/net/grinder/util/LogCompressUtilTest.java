package net.grinder.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class LogCompressUtilTest {
	@Test
	public void testLogCompressUnCompress() throws IOException {
		File file = new File(LogCompressUtilTest.class.getResource("/grinder1.properties").getFile());
		byte[] zipedContent = LogCompressUtil.compressFile(file);
		FileUtils.writeByteArrayToFile(new File("c:/Project/a.zip"), zipedContent);
		File createTempFile = File.createTempFile("a22", "tmp");
		LogCompressUtil.unCompress(zipedContent, createTempFile);
		assertThat(createTempFile.exists(), is(true));
		byte[] unzipedContent = FileUtils.readFileToByteArray(createTempFile);
		assertThat(FileUtils.readFileToByteArray(file), is(unzipedContent));
		
	}
}
