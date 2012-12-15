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
package org.ngrinder.monitor.controller.domain;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Collection of {@link MonitorCollectionInfoDomain}.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class MonitorCollection {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorCollection.class);
	private List<MonitorCollectionInfoDomain> collectionList = new ArrayList<MonitorCollectionInfoDomain>();

	private static final MonitorCollection SYSTEM_MONITOR_COLLECTION_INSTANCE = new MonitorCollection();

	static {
		SYSTEM_MONITOR_COLLECTION_INSTANCE.addSystemMonitorCollection();
	}

	private MonitorCollection() {
	}

	static MonitorCollection getSystemMonitorCollection() {
		return SYSTEM_MONITOR_COLLECTION_INSTANCE;
	}

	private void addSystemMonitorCollection() {
		try {
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.SYSTEM;
			ObjectName systemName = new ObjectName(objNameStr);
			collectionList.add(new MonitorCollectionInfoDomain(systemName, "SystemInfo", SystemInfo.class));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public List<MonitorCollectionInfoDomain> getMXBean() {
		return collectionList;
	}
}
