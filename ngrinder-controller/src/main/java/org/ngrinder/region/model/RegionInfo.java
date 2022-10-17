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
package org.ngrinder.region.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/**
 * Region info to be shared b/w controllers.
 *
 * @since 3.1
 */
@Getter
@SuppressWarnings("UnusedDeclaration")
public class RegionInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Integer port;

	private String regionName;

	private Set<String> subregion;

	@Setter
	private String ip;

	@Setter
	private boolean visible = true;

	/**
	 * Constructor with true visibility.
	 *
	 * @param ip    ip
	 * @param port  port
	 */
	public RegionInfo(String ip, int port) {
		this(ip, port, true);
	}

	/**
	 * Constructor.
	 *
	 * @param ip              ip
	 * @param visible         true if visible
	 */
	public RegionInfo(String ip, boolean visible) {
		this(ip, null, visible);
	}

	/**
	 * Constructor.
	 *
	 * @param ip              ip
	 * @param port  	      port
	 * @param visible         true if visible
	 */
	public RegionInfo(String ip, Integer port, boolean visible) {
		this.ip = ip;
		this.port = port;
		this.visible = visible;
	}

	public RegionInfo(String regionName, String ip, Integer port) {
		this.regionName = regionName;
		this.ip = ip;
		this.port = port;
	}

	public RegionInfo(String regionName, Set<String> subregion , String ip, Integer port) {
		this.regionName = regionName;
		this.subregion = subregion;
		this.ip = ip;
		this.port = port;
	}

}
