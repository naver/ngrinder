/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check every now and then that a certain file has not changed. If it has, then
 * call the {@link #doOnChange} method.
 * 
 * @author JunHo Yoon
 * @since 3.1.1
 */
public abstract class FileWatchdog extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileWatchdog.class);
	/**
	 * The default delay between every file modification check, set to 60
	 * seconds.
	 */
	public static final long DEFAULT_DELAY = 60000;
	/**
	 * The name of the file to observe for changes.
	 */
	private String filename;

	/**
	 * The delay to observe between every check. By default set
	 * {@link #DEFAULT_DELAY}.
	 */
	private long delay = DEFAULT_DELAY;

	private File file;
	private long lastModified = 0;
	private boolean warnedAlready = false;
	private boolean interrupted = false;

	protected FileWatchdog(String filename) {
		this.filename = filename;
		file = new File(filename);
		setDaemon(true);
		checkAndConfigure();
	}

	/**
	 * Set the delay to observe between each check of the file changes.
	 * 
	 * @param delay
	 *            the frequency of file watch.
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * abstract method to be run when the file is changed.
	 */
	protected abstract void doOnChange();

	protected void checkAndConfigure() {
		boolean fileExists;
		try {
			fileExists = file.exists();
		} catch (SecurityException e) {
			LOGGER.warn("Was not allowed to read check file existence, file:[" + filename + "].");
			interrupted = true; // there is no point in continuing
			return;
		}

		if (fileExists) {
			long l = file.lastModified(); // this can also throw a
			if (lastModified ==0) {
				lastModified = l; // is very unlikely.
			}
			if (l > lastModified) { // however, if we reached this point this
				lastModified = l; // is very unlikely.
				doOnChange();
				warnedAlready = false;
			}
		} else {
			if (!warnedAlready) {
				LOGGER.debug("[" + filename + "] does not exist.");
				warnedAlready = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (!interrupted && !isInterrupted()) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				NoOp.noOp();
			}
			checkAndConfigure();
		}
	}
}
