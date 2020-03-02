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
package org.ngrinder.monitor.share.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Disk Usage Calculation Class. This class get the Disk Read and Write so far and calc ReadPerSec, WritePerSec.
 *
 * @author lingj
 *
 */
public class DiskBusy implements Serializable {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 7655104078722834344L;

	private long time;

	/**
	 * Default constructor.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public DiskBusy() {
	}

	/**
	 * Constructor with the timestamp.
	 *
	 * @param time	current timestamp.
	 */
	public DiskBusy(long time) {
		this.time = time;
	}

	private long read;
	private long write;

	private long readPerSec;
	private long writePerSec;

	/**
	 * Calculate the diskBusy by subtracting prev diskBusy.
	 *
	 * @param diskBusy	diskBusy adjusted against.
	 * @return adjusted diskBusy.
	 */
	public DiskBusy adjust(DiskBusy diskBusy) {
		float rate = ((float) Math.abs(time - diskBusy.getTime())) / 1000;
		readPerSec = ((long) ((read - diskBusy.getRead()) / rate));
		writePerSec = ((long) ((write - diskBusy.getWrite()) / rate));
		return this;
	}

	public long getTime() {
		return time;
	}

	public long getWritePerSec() {
		return writePerSec;
	}

	public void setWritePerSec(long writePerSec) {
		this.writePerSec = writePerSec;
	}

	public long getReadPerSec() {
		return readPerSec;
	}

	public void setReadPerSec(long readPerSec) {
		this.readPerSec = readPerSec;
	}

	public long getRead() {
		return read;
	}

	public void setRead(long read) {
		this.read = read;
	}

	public long getWrite() {
		return write;
	}

	public void setWrite(long write) {
		this.write = write;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
