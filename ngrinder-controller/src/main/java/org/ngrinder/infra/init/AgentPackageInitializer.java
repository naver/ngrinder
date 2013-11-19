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
package org.ngrinder.infra.init;

import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.infra.annotation.RuntimeOnlyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

/**
 * Create Agent Package
 *
 * This class is used to create agent package which located at {NGRINDER_HOME}/update_agent folder
 * when system initialized firstly.
 *
 * @author Matt
 * @since 3.3
 */
@RuntimeOnlyComponent
public class AgentPackageInitializer {

    private static File agentFile;
    @Autowired
    private AgentManagerService agentManagerService;

    public static File getAgentPackageFile() {
        return agentFile;
    }

    /**
     * Create agent package.
     */
    @PostConstruct
    @Async
    public void init() throws IOException {
        agentFile = agentManagerService.createAgentPackage((URLClassLoader) getClass().getClassLoader());
    }
}
