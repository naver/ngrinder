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
package org.ngrinder.monitor.domain;

import javax.management.ObjectName;

/**
 * Monitor collection info domain.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class MonitorCollectionInfoDomain {
	private ObjectName objectName;
	private String attrName;

	/**
	 * Constructor for the collection info.
	 *
	 * @param objectName  is the object name related with JMX domain name
	 * @param attrName    is the attribute name in this domain, used to get concrete monitor data
	 */
	public MonitorCollectionInfoDomain(ObjectName objectName, String attrName) {
		this.objectName = objectName;
		this.attrName = attrName;
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public String getAttrName() {
		return attrName;
	}

}
