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
package org.ngrinder.agent.controller;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.init.AgentPackageInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Agent Download Controller.
 * 
 * @author Tobi
 * @since 3.0
 */
@Controller
@RequestMapping("/agent")
public class AgentDownloadController extends NGrinderBaseController {

	@Autowired
	private Config config;

	/**
     * Download agent.
     *
     * @param fileName
     *            file path of agent
     * @param response
     *            response.
     */
    @RequestMapping(value = "/download/{fileName:[a-zA-Z0-9\\.\\-]+}")
    public void downloadAgent(@PathVariable String fileName, HttpServletResponse response) {
        File ngrinderFile = new File(config.getHome().getDownloadDirectory(), fileName);
        FileDownloadUtil.downloadFile(response, ngrinderFile);
    }

    /**
     * Download agent's latest version.
     * @param response
     *            response.
     */
    @RequestMapping(value = "/download_new_agent")
    public void downloadLatestAgent(HttpServletResponse response) {
        FileDownloadUtil.downloadFile(response, AgentPackageInitializer.getAgentPackageFile());
    }

}
