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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Network Usage Calculation Class. This class get the bandwidth so far and calc RX, TX.
 * 
 * @author JunHo Yoon
 * 
 */
public class BandWidth implements Serializable {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 7655104078722834344L;

	private long time;

	/**
	 * Default constructor.
	 */
	public BandWidth() {
	}

	/**
	 * Constructor with the timestamp.
	 * 
	 * @param time
	 *            current timestamp.
	 */
	public BandWidth(long time) {
		this.time = time;
	}

	private long recieved;
	private long sent;

	private long recivedPerSec;
	private long sentPerSec;

	/**
	 * Calculate the bandWith by subtracting prev bandwidth.
	 * 
	 * @param bandWidth
	 *            bandWidth adjusted against.
	 * @return adjusted bandWidth.
	 */
	public BandWidth adjust(BandWidth bandWidth) {
		float rate = ((float) Math.abs(time - bandWidth.getTime())) / 1000;
		recivedPerSec = ((long) ((recieved - bandWidth.getRecieved()) * rate));
		sentPerSec = ((long) ((sent - bandWidth.getSent()) * rate));
		return this;
	}

	public long getTime() {
		return time;
	}

	public long getSentPerSec() {
		return sentPerSec;
	}

	public void setSentPerSec(long sentPerSec) {
		this.sentPerSec = sentPerSec;
	}

	public long getRecivedPerSec() {
		return recivedPerSec;
	}

	public void setRecivedPerSec(long recivedPerSec) {
		this.recivedPerSec = recivedPerSec;
	}

	public long getRecieved() {
		return recieved;
	}

	public void setRecieved(long recieved) {
		this.recieved = recieved;
	}

	public long getSent() {
		return sent;
	}

	public void setSent(long sent) {
		this.sent = sent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}