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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileLock;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utilities.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
public abstract class FileUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * Write object into file.
	 * 
	 * @param file
	 *            file
	 * @param obj
	 *            obj to be written.
	 */
	public static void writeObjectToFile(File file, Object obj) {
		FileOutputStream fout = null;
		ObjectOutputStream oout = null;
		ByteArrayOutputStream bout = null;
		FileLock lock = null;
		try {
			bout = new ByteArrayOutputStream();
			oout = new ObjectOutputStream(bout);
			oout.writeObject(obj);
			oout.flush();

			byte[] byteArray = bout.toByteArray();
			fout = new FileOutputStream(file, false);
			lock = fout.getChannel().lock();
			fout.write(byteArray);

		} catch (Exception e) {
			LOGGER.error("IO error for file {} : {}", file, e.getMessage());
			LOGGER.debug("Details : ", e);
		} finally {
			if (lock != null) {
				try {
					lock.release();
				} catch (Exception e) {
					LOGGER.error("unlocking is failed for file {}", file, e);
				}
			}
			IOUtils.closeQuietly(bout);
			IOUtils.closeQuietly(fout);
			IOUtils.closeQuietly(oout);
		}
	}

	/**
	 * Read object from file.
	 * 
	 * @param file
	 *            file
	 * @param defaultValue
	 *            default value
	 * @return read object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readObjectFromFile(File file, T defaultValue) {
		if (!file.exists()) {
			return defaultValue;
		}
		FileInputStream fin = null;
		ObjectInputStream oin = null;
		ByteArrayInputStream bin = null;
		FileLock lock = null;
		try {
			fin = new FileInputStream(file);
			lock = fin.getChannel().lock(0, Long.MAX_VALUE, true);
			byte[] byteArray = IOUtils.toByteArray(fin);
			bin = new ByteArrayInputStream(byteArray);
			oin = new ObjectInputStream(bin);
			Object readObject = oin.readObject();
			return (readObject == null) ? defaultValue : (T) readObject;

		} catch (Exception e) {
			LOGGER.error("IO error for file {} : {}", file, e.getMessage());
			LOGGER.debug("Details : ", e);
		} finally {
			if (lock != null) {
				try {
					lock.release();
				} catch (Exception e) {
					LOGGER.error("unlocking is failed for file {}", file, e);
				}
			}
			IOUtils.closeQuietly(bin);
			IOUtils.closeQuietly(fin);
			IOUtils.closeQuietly(oin);
		}
		return defaultValue;
	}

}
