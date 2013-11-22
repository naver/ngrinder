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
package org.ngrinder.agent.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.AgentControllerCommunicationDefaults;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.service.IAgentManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ngrinder.agent.repository.AgentManagerSpecification.active;
import static org.ngrinder.agent.repository.AgentManagerSpecification.visible;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.CompressionUtil.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.TypeConvertUtil.cast;

/**
 * Agent manager service.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentManagerService implements IAgentManagerService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentManagerService.class);

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerRepository agentManagerRepository;

	@Autowired
	private Config config;

	/**
	 * Run a scheduled task to check the agent status periodically.
	 * <p/>
	 * This method updates the agent statuses in DB.
	 *
	 * @since 3.1
	 */
	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void checkAgentStateRegularly() {
		checkAgentState();
	}

	protected void checkAgentState() {
		List<AgentInfo> changeAgents = Lists.newArrayList();
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		Map<String, AgentControllerIdentityImplementation> attachedAgentMap = Maps.newHashMap();
		for (AgentIdentity agentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = cast(agentIdentity);
			attachedAgentMap.put(createAgentKey(agentControllerIdentity), agentControllerIdentity);
		}


		// If region is not specified retrieved all
		List<AgentInfo> agentsInDB = getAgentRepository().findAll();

		Multimap<String, AgentInfo> agentInDBMap = ArrayListMultimap.create();
		// step1. check all agents in DB, whether they are attached to
		// controller.
		for (AgentInfo each : agentsInDB) {
			agentInDBMap.put(createAgentKey(each), each);
		}

		List<AgentInfo> agentsToBeDeleted = Lists.newArrayList();
		for (String entryKey : agentInDBMap.keySet()) {
			Collection<AgentInfo> collection = agentInDBMap.get(entryKey);
			int count = 0;
			AgentInfo interestingAgentInfo = null;
			for (AgentInfo each : collection) {
				// Just select one element and delete others.
				if (count++ == 0) {
					interestingAgentInfo = each;
				} else {
					agentsToBeDeleted.add(each);
				}
			}
			if (interestingAgentInfo == null) {
				continue;
			}

			AgentControllerIdentityImplementation agentIdentity = attachedAgentMap.remove(entryKey);
			if (agentIdentity == null) {
				// this agent is not attached to controller
				interestingAgentInfo.setState(AgentControllerState.INACTIVE);
			} else {
				interestingAgentInfo.setState(getAgentManager().getAgentState(agentIdentity));
				interestingAgentInfo.setRegion(agentIdentity.getRegion());
				interestingAgentInfo.setPort(getAgentManager().getAgentConnectingPort(agentIdentity));
			}
			changeAgents.add(interestingAgentInfo);
		}

		// step2. check all attached agents, whether they are new, and not saved
		// in DB.
		for (AgentControllerIdentityImplementation agentIdentity : attachedAgentMap.values()) {
			changeAgents.add(fillUp(new AgentInfo(), agentIdentity));
		}

		// step3. update into DB
		getAgentRepository().save(changeAgents);
		getAgentRepository().delete(agentsToBeDeleted);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#getUserAvailableAgentCountMap
	 * (org.ngrinder .model.User)
	 */
	@Override
	public Map<String, MutableInt> getUserAvailableAgentCountMap(User user) {
		int availableShareAgents = 0;
		int availableUserOwnAgent = 0;
		String myAgentSuffix = "owned_" + user.getUserId();
		for (AgentInfo agentInfo : getAllActiveAgentInfoFromDB()) {
			// Skip all agents which are disapproved, inactive or
			// have no region prefix.
			if (!agentInfo.isApproved()) {
				continue;
			}
			String fullRegion = agentInfo.getRegion();

			// It's this controller's agent
			if (fullRegion.endsWith(myAgentSuffix)) {
				availableUserOwnAgent++;
			} else if (fullRegion.contains("owned_")) {
				// If it's the other controller's agent.. skip..
				continue;
			} else {
				availableShareAgents++;
			}
		}

		int maxAgentSizePerConsole = getMaxAgentSizePerConsole();

		availableShareAgents = (Math.min(availableShareAgents, maxAgentSizePerConsole));

		Map<String, MutableInt> result = new HashMap<String, MutableInt>(1);
		result.put(Config.NONE_REGION, new MutableInt(availableShareAgents + availableUserOwnAgent));
		return result;
	}

	int getMaxAgentSizePerConsole() {
		return getAgentManager().getMaxAgentSizePerConsole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.agent.service.IAgentManagerService#getLocalAgents()
	 */
	@Override
	@Transactional
	public List<AgentInfo> getLocalAgents() {
		Map<String, AgentInfo> agentInfoMap = createLocalAgentMapFromDB();
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		List<AgentInfo> agentList = new ArrayList<AgentInfo>(allAttachedAgents.size());
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = cast(eachAgentIdentity);
			agentList.add(createAgentInfo(agentControllerIdentity, agentInfoMap));
		}
		return agentList;
	}

	private Map<String, AgentInfo> createLocalAgentMapFromDB() {
		Map<String, AgentInfo> agentInfoMap = Maps.newHashMap();
		for (AgentInfo each : getLocalAgentListFromDB()) {
			agentInfoMap.put(createAgentKey(each), each);
		}
		return agentInfoMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#createAgentKey(org.ngrinder
	 * .agent.model.AgentInfo )
	 */
	@Override
	public String createAgentKey(AgentInfo agentInfo) {
		return createAgentKey(agentInfo.getIp(), agentInfo.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#createAgentKey(net.grinder
	 * .engine.controller .AgentControllerIdentityImplementation)
	 */
	@Override
	public String createAgentKey(AgentControllerIdentityImplementation agentIdentity) {
		return createAgentKey(agentIdentity.getIp(), agentIdentity.getName());
	}

	protected String createAgentKey(String ip, String name) {
		return ip + "_" + name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.agent.service.IAgentManagerService#
	 * getLocalAgentIdentityByIpAndName(java.lang .String, java.lang.String)
	 */
	@Override
	public AgentControllerIdentityImplementation getLocalAgentIdentityByIpAndName(String ip, String name) {
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentIdentity = cast(eachAgentIdentity);
			if (StringUtils.equals(ip, agentIdentity.getIp()) && StringUtils.equals(name, agentIdentity.getName())) {
				return agentIdentity;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#getLocalAgentListFromDB()
	 */
	@Override
	public List<AgentInfo> getLocalAgentListFromDB() {
		return getAgentRepository().findAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#getAllActiveAgentInfoFromDB
	 * ()
	 */
	@Override
	public List<AgentInfo> getAllActiveAgentInfoFromDB() {
		return getAgentRepository().findAll(active());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#getAllVisibleAgentInfoFromDB
	 * ()
	 */
	@Override
	public List<AgentInfo> getAllVisibleAgentInfoFromDB() {
		return getAgentRepository().findAll(visible());
	}

	private AgentInfo createAgentInfo(AgentControllerIdentityImplementation agentIdentity,
	                                  Map<String, AgentInfo> agentInfoMap) {
		AgentInfo agentInfo = agentInfoMap.get(createAgentKey(agentIdentity));
		if (agentInfo == null) {
			agentInfo = new AgentInfo();
		}
		return fillUp(agentInfo, agentIdentity);
	}

	protected AgentInfo fillUp(AgentInfo agentInfo, AgentControllerIdentityImplementation agentIdentity) {
		if (agentIdentity != null) {
			agentInfo.setAgentIdentity(agentIdentity);
			agentInfo.setName(agentIdentity.getName());
			agentInfo.setRegion(agentIdentity.getRegion());
			agentInfo.setIp(agentIdentity.getIp());
			AgentManager agentManager = getAgentManager();
			agentInfo.setPort(agentManager.getAgentConnectingPort(agentIdentity));
			agentInfo.setState(agentManager.getAgentState(agentIdentity));
		}
		return agentInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.agent.service.IAgentManagerService#getAgent(long,
	 * boolean)
	 */
	@Override
	public AgentInfo getAgent(long id, boolean includeAgentIdentity) {
		AgentInfo findOne = getAgentRepository().findOne(id);
		if (findOne == null) {
			return null;
		}
		if (includeAgentIdentity) {
			AgentControllerIdentityImplementation agentIdentityByIp = getLocalAgentIdentityByIpAndName(findOne.getIp(),
					findOne.getName());
			return fillUp(findOne, agentIdentityByIp);
		} else {
			return findOne;
		}
	}

	/**
	 * Save agent.
	 *
	 * @param agent saved agent
	 */
	public void saveAgent(AgentInfo agent) {
		getAgentRepository().save(agent);
	}

	/**
	 * Delete agent.
	 *
	 * @param id agent id to be deleted
	 */
	public void deleteAgent(long id) {
		getAgentRepository().delete(id);
	}

	/**
	 * Approve/Unapprove the agent on given id.
	 *
	 * @param id      id
	 * @param approve true/false
	 */
	@Transactional
	public void approve(Long id, boolean approve) {
		AgentInfo found = getAgentRepository().findOne(id);
		if (found != null) {
			found.setApproved(approve);
			getAgentRepository().save(found);
			getAgentRepository().findOne(found.getId());
		}

	}

	/**
	 * Stop agent. If it's in cluster mode, it queue to agentRequestCache.
	 * otherwise, it send stop message to the agent.
	 *
	 * @param id identity of agent to stop.
	 */
	@Transactional
	public void stopAgent(Long id) {
		AgentInfo agent = getAgent(id, true);
		if (agent == null) {
			return;
		}
		getAgentManager().stopAgent(agent.getAgentIdentity());
	}

	/**
	 * Add the agent system data model share request on cache.
	 *
	 * @param id agent id.
	 */
	public void requestShareAgentSystemDataModel(Long id) {
		noOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#getAgentSystemDataModel
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public SystemDataModel getAgentSystemDataModel(String ip, String name) {
		AgentControllerIdentityImplementation agentIdentity = getLocalAgentIdentityByIpAndName(ip, name);
		return agentIdentity != null ? getAgentManager().getSystemDataModel(agentIdentity) : new SystemDataModel();
	}

	AgentManager getAgentManager() {
		return agentManager;
	}

	void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	AgentManagerRepository getAgentRepository() {
		return agentManagerRepository;
	}

	void setAgentRepository(AgentManagerRepository agentRepository) {
		this.agentManagerRepository = agentRepository;
	}

	protected AgentManagerRepository getAgentManagerRepository() {
		return this.agentManagerRepository;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Get the agent package containing folder.
	 *
	 * @return agent package folder
	 */
	public File getAgentPackagesDir() {
		return config.getHome().getSubFile("download");
	}

	static class TarArchivingZipEntryProcessor implements ZipEntryProcessor {
		private TarArchiveOutputStream tao;
		private String basePath;
		private int mode;

		TarArchivingZipEntryProcessor(TarArchiveOutputStream tao, String basePath, int mode) {
			this.tao = tao;
			this.basePath = basePath;
			this.mode = mode;
		}

		@Override
		public void process(ZipFile file, ZipEntry entry) throws IOException {
			InputStream inputStream = null;
			try {
				inputStream = file.getInputStream(entry);
				addInputStreamToTar(this.tao, inputStream, basePath + FilenameUtils.getName(entry.getName()),
						entry.getSize(),
						this.mode);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

	/*
	* (non-Javadoc)
	*
	* @see
	* org.ngrinder.agent.service.IAgentManagerService#createAgentPackage
	*/
	public File createAgentPackage(URLClassLoader classLoader) throws IOException, URISyntaxException {
		File agentPackagesDir = getAgentPackagesDir();
		agentPackagesDir.mkdirs();
		File agentTar = new File(agentPackagesDir, getDistributionPackageName("ngrinder-core", false));
		if (!config.isTestMode() && agentTar.exists()) {
			return agentTar;
		}
		FileUtils.deleteQuietly(agentTar);
		final String basePath = "ngrinder-agent/";
		final String libPath = basePath + "lib/";
		TarArchiveOutputStream tarOutputStream = null;
		try {
			tarOutputStream = createGzippedTarArchiveStream(agentTar);
			addFolderToTar(tarOutputStream, basePath);
			addFolderToTar(tarOutputStream, libPath);
			Set<String> libs = getDependentLibs(classLoader);

			for (URL eachUrl : classLoader.getURLs()) {
				File eachClassPath = new File(eachUrl.getFile());
				if (!isJar(eachClassPath)) {
					continue;
				}
				if (isAgentDependentLib(eachClassPath, "ngrinder-sh")) {
					processJarEntries(eachClassPath, new TarArchivingZipEntryProcessor(tarOutputStream, basePath, 0100755));
				} else if (isAgentDependentLib(eachClassPath, libs)) {
					addFileToTar(tarOutputStream, eachClassPath, libPath + eachClassPath.getName());
				}
			}
			addAgentConfToTar(tarOutputStream, basePath);
		} catch (IOException e) {
			LOGGER.error("Error while generating an agent package" + e.getMessage());
		} finally {
			IOUtils.closeQuietly(tarOutputStream);
		}
		return agentTar;
	}

	private TarArchiveOutputStream createGzippedTarArchiveStream(File agentTar) throws IOException {
		FileOutputStream fos = new FileOutputStream(agentTar);
		return new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)));
	}

	private void addAgentConfToTar(TarArchiveOutputStream tarOutputStream, String basePath) throws IOException {
		final String config = getAgentConfigContent("agent_agent.conf", getAgentConfigParam());
		final byte[] bytes = config.getBytes();
		addInputStreamToTar(tarOutputStream, new ByteArrayInputStream(bytes), basePath + "__agent.conf",
				bytes.length, TarArchiveEntry.DEFAULT_FILE_MODE);
	}

	private Set<String> getDependentLibs(URLClassLoader cl) throws IOException {
		Set<String> libs = new HashSet<String>();
		final String dependencies = IOUtils.toString(cl.getResourceAsStream("dependencies.txt"));
		for (String each : StringUtils.split(dependencies, ",;")) {
			libs.add(each.trim());
		}
		libs.add(getPackageName("ngrinder-core") + ".jar");
		return libs;
	}

	private Map<String, Object> getAgentConfigParam() {
		Map<String, Object> confMap = newHashMap();
		confMap.put("controllerIP", config.getCurrentIP());
		final int port = config.getSystemProperties()
				.getPropertyInt(NGrinderConstants.NGRINDER_PROP_AGENT_CONTROL_PORT,
						AgentControllerCommunicationDefaults.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		confMap.put("controllerPort", String.valueOf(port));
		confMap.put("controllerRegion", config.getRegion());
		return confMap;
	}


	/**
	 * Get the agent.config content replacing the variables with the given values.
	 *
	 * @param values map of configurations.
	 * @return generated string
	 */

	public String getAgentConfigContent(String templateName, Map<String, Object> values) {
		StringWriter writer = null;
		try {
			Configuration config = new Configuration();
			ClassPathResource cpr = new ClassPathResource("ngrinder_agent_home_template");
			config.setDirectoryForTemplateLoading(cpr.getFile());
			config.setObjectWrapper(new DefaultObjectWrapper());
			Template template = config.getTemplate(templateName);
			writer = new StringWriter();
			template.process(values, writer);
			return writer.toString();
		} catch (Exception e) {
			throw processException("Error while fetching the script template.", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#updateAgentLib
	 * (java.lang.String)
	 */
	@Override
	public void updateAgent(List<Long> ids) {
		for (Long each : ids) {
			updateAgent(each);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ngrinder.agent.service.IAgentManagerService#updateAgentLib
	 * (java.lang.String)
	 */
	@Override
	public void updateAgent(Long id) {
		AgentInfo agent = getAgent(id, true);
		if (agent == null) {
			return;
		}
		agentManager.updateAgent(agent.getAgentIdentity(), config.getVersion());
	}

	/**
	 * Get package name
	 *
	 * @param moduleName nGrinder module name.
	 * @return String module full name.
	 */
	public String getPackageName(String moduleName) {
		return moduleName + "-" + config.getVersion();
	}

	/**
	 * Get distributable package name with appropriate extension.
	 *
	 * @param moduleName nGrinder sub  module name.
	 * @param forWindow  if true, then package type is zip,if false, package type is tar.gz.
	 * @return String  module full name.
	 */
	public String getDistributionPackageName(String moduleName, boolean forWindow) {
		return getPackageName(moduleName) + (forWindow ? ".zip" : ".tar.gz");
	}

	/**
	 * Check if this given path is jar.
	 *
	 * @param libFile lib file
	 * @return true if it's jar
	 */
	public boolean isJar(File libFile) {
		return StringUtils.endsWith(libFile.getName(), ".jar");
	}

	/**
	 * Check if this given lib file is the given library.
	 *
	 * @param libFile lib file
	 * @param libName desirable name
	 * @return true if dependent lib
	 */
	public boolean isAgentDependentLib(File libFile, String libName) {
		return StringUtils.startsWith(libFile.getName(), libName);
	}

	/**
	 * Check if this given lib file in the given lib set.
	 *
	 * @param libFile lib file
	 * @param libs    lib set
	 * @return true if dependent lib
	 */
	public boolean isAgentDependentLib(File libFile, Set<String> libs) {
		return libs.contains(libFile.getName());
	}
}
