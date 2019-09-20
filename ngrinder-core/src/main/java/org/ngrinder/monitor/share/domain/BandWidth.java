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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Network Usage Calculation Class. This class get the bandwidth so far and calc RX, TX.
 * 
 * @author JunHo Yoon
 * 
 */
@Getter
@Setter
@ToString
public class BandWidth implements Serializable {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 7655104078722834344L;

	@Setter(AccessLevel.NONE)
	private long time;

	/**
	 * Default constructor.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public BandWidth() {
	}

	/**
	 * Constructor with the timestamp.
	 * 
	 * @param time	current timestamp.
	 */
	public BandWidth(long time) {
		this.time = time;
	}

	private long received;
	private long sent;

	private long receivedPerSec;
	private long sentPerSec;

	/**
	 * Calculate the bandWith by subtracting prev bandwidth.
	 * 
	 * @param bandWidth	bandWidth adjusted against.
	 * @return adjusted bandWidth.
	 */
	public BandWidth adjust(BandWidth bandWidth) {
		float rate = ((float) Math.abs(time - bandWidth.getTime())) / 1000;
		receivedPerSec = ((long) ((received - bandWidth.getReceived()) / rate));
		sentPerSec = ((long) ((sent - bandWidth.getSent()) / rate));
		return this;
	}

}
