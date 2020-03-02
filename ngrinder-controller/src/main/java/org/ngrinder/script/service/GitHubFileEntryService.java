package org.ngrinder.script.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.*;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.exception.PerfTestPrepareException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.GitHubConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.constant.CacheConstants.CACHE_GITHUB_IS_MAVEN_GROOVY;
import static org.ngrinder.common.constant.CacheConstants.CACHE_GITHUB_SCRIPTS;
import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_GITHUB_BASE_URL;
import static org.ngrinder.common.util.AopUtils.proxy;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.JsonUtils.deserialize;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.PathUtils.removePrependedSlash;
import static org.ngrinder.common.util.TypeConvertUtils.cast;
import static org.ngrinder.script.model.FileType.getFileTypeByName;
import static org.tmatesoft.svn.core.SVNDepth.INFINITY;
import static org.tmatesoft.svn.core.SVNURL.parseURIEncoded;
import static org.tmatesoft.svn.core.wc.SVNClientManager.newInstance;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

/**
 * @since 3.5.0
 */
@Slf4j
@Service
public class GitHubFileEntryService {

	private static final String GITHUB_CONFIG_NAME = ".gitconfig.yml";

	private final FileEntryService fileEntryService;

	private final ObjectMapper objectMapper;

	private final Config config;

	private final PerfTestService perfTestService;

	private static final String MAVEN_PATH = "/src/main/java";

	private static final RateLimitHandlerEx rateLimitHandlerEx = new RateLimitHandlerEx();

	public GitHubFileEntryService(FileEntryService fileEntryService, @Lazy ObjectMapper objectMapper,
								  Config config, @Lazy PerfTestService perfTestService) {
		this.fileEntryService = fileEntryService;
		this.objectMapper = objectMapper;
		this.config = config;
		this.perfTestService = perfTestService;
	}

	public FileEntry getOne(GHRepository ghRepository, GitHubConfig gitHubConfig, String scriptPath) {
		String fullPath = getCheckoutDirPath(ghRepository, gitHubConfig, scriptPath);
		if (proxy(this).isGroovyMavenProject(ghRepository, scriptPath)) {
			fullPath += scriptPath.substring(scriptPath.indexOf(MAVEN_PATH));
			FileEntry fileEntry = createGitHubScriptFileEntry(fullPath);
			fileEntry.getProperties().put("type", "groovy-maven");
			fileEntry.getProperties().put("scriptPath", scriptPath);
			return fileEntry;
		} else {
			fullPath += getName(scriptPath);
			return createGitHubScriptFileEntry(fullPath);
		}
	}

	public void checkoutGitHubScript(PerfTest perfTest, GHRepository ghRepository, GitHubConfig gitHubConfig) {
		String activeBranch = "";
		try {
			String defaultBranch = ghRepository.getDefaultBranch();
			String configuredBranch = gitHubConfig.getBranch();
			activeBranch = defaultBranch;

			if (!isEmpty(configuredBranch)) {
				activeBranch = configuredBranch;
			}
			String sha = ghRepository.getBranch(activeBranch).getSHA1();
			String scriptPath = perfTest.getScriptName();

			String checkoutDirPath = getCheckoutDirPath(ghRepository, gitHubConfig, scriptPath);
			SVNUpdateClient svnUpdateClient = createSvnUpdateClient(gitHubConfig);
			File checkoutDir = new File(checkoutDirPath);
			SVNURL checkoutUrl = createCheckoutUrl(ghRepository, scriptPath, configuredBranch, isDefaultBranch(configuredBranch, defaultBranch));

			perfTestService.markProgressAndStatus(perfTest, Status.CHECKOUT_SCRIPT, "Getting script from github.");
			if (!isSvnWorkingCopyDir(checkoutDir)) {
				svnUpdateClient.doCheckout(checkoutUrl, checkoutDir, HEAD, HEAD, INFINITY, true);
				saveSha(sha, checkoutDirPath);
				log.info("github checkout to: {}, url: {} sha: {}", checkoutDir, checkoutUrl.toString(), sha);
			} else {
				if (!isSameRevision(sha, checkoutDirPath)) {
					svnUpdateClient.doSwitch(checkoutDir, checkoutUrl, HEAD, HEAD, INFINITY, true, true);
					saveSha(sha, checkoutDirPath);
					log.info("github update to: {}, sha: {}", checkoutDir, sha);
				}
			}
			perfTest.setScriptRevision(createScriptRevisionUrl(ghRepository.getSvnUrl(), sha, scriptPath));
		} catch (Exception e) {
			throw new PerfTestPrepareException("Failed to checkout scripts from github.\n" +
				"Please check your github configuration.\n\n" + e.getMessage(), e);
		}
	}

	private String createScriptRevisionUrl(String baseUrl, String treeSha, String scriptPath) {
		return baseUrl + "/blob/" + treeSha + "/" + scriptPath;
	}

	private SVNUpdateClient createSvnUpdateClient(GitHubConfig gitHubConfig) {
		// userName is don't care parameter if using access token.
		BasicAuthenticationManager basicAuthenticationManager
			= new BasicAuthenticationManager("ngrinder", gitHubConfig.getAccessToken());

		SVNClientManager svnClientManager = newInstance();
		svnClientManager.setAuthenticationManager(basicAuthenticationManager);
		return svnClientManager.getUpdateClient();
	}

	private boolean isDefaultBranch(String configuredBranch, String defaultBranch) {
		return isEmpty(configuredBranch) || configuredBranch.equals(defaultBranch);
	}

	private boolean isSvnWorkingCopyDir(File directory) {
		if (!directory.exists() || !directory.isDirectory()) {
			return false;
		}
		return new File(directory.getPath() + "/" + ".svn").exists();
	}

	@Cacheable(value = CACHE_GITHUB_IS_MAVEN_GROOVY, key = "#scriptPath")
	public boolean isGroovyMavenProject(GHRepository ghRepository, String scriptPath) {
		if (!scriptPath.contains(MAVEN_PATH)) {
			return false;
		}

		try {
			List<GHContent> ghContents = ghRepository.getDirectoryContent(scriptPath.split(MAVEN_PATH)[0]);
			return ghContents.stream().anyMatch(ghContent -> ghContent.getName().equals("pom.xml"));
		} catch (IOException e) {
			return false;
		}
	}

	private FileEntry createGitHubScriptFileEntry(String fullPath) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setFileType(getFileTypeByName(fullPath));
		fileEntry.setPath(fullPath);
		fileEntry.setRevision(-1L);
		fileEntry.setProperties(buildMap("scm", "github"));
		return fileEntry;
	}

	private void saveSha(String sha, String checkoutDirPath) throws IOException {
		File shaFile = new File(checkoutDirPath + "/sha");
		writeStringToFile(shaFile, sha, UTF_8);
	}

	private boolean isSameRevision(String sha, String checkoutDirPath) throws IOException {
		File shaFile = new File(checkoutDirPath + "/sha");
		if (!shaFile.exists()) {
			return false;
		}

		String oldSha = readFileToString(shaFile, UTF_8).trim();
		return StringUtils.equals(sha, oldSha);
	}

	private String getCheckoutDirPath(GHRepository ghRepository, GitHubConfig gitHubConfig, String scriptPath) {
		try {
			String checkoutScriptPath;
			URI uri = new URI(getGitHubBaseUrl(gitHubConfig));
			if (proxy(this).isGroovyMavenProject(ghRepository, scriptPath)) {
				checkoutScriptPath = scriptPath.split(MAVEN_PATH)[0];
			} else {
				checkoutScriptPath = getFullPath(scriptPath);
			}
			return config.getHome().getDirectory().getPath() + "/github/" + uri.getHost()
				+ "/" + gitHubConfig.getOwner() + "/" + gitHubConfig.getRepo() + "/" + checkoutScriptPath;
		} catch (URISyntaxException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	/**
	 * get all github configuration of {@link User}.
	 *
	 * @param user user.
	 * @return list of github configuration.
	 * @since 3.5.0
	 */
	public List<GitHubConfig> getAllGitHubConfig(User user) throws FileNotFoundException {
		FileEntry gitConfigYaml = fileEntryService.getOne(user, GITHUB_CONFIG_NAME, -1L);
		if (gitConfigYaml == null) {
			throw new FileNotFoundException(GITHUB_CONFIG_NAME + " isn't exist.");
		}

		return getAllGithubConfig(gitConfigYaml);
	}

	private List<GitHubConfig> getAllGithubConfig(FileEntry gitConfigYaml) {
		List<GitHubConfig> gitHubConfig = new ArrayList<>();
		// Yaml is not thread safe. so create it every time.
		Yaml yaml = new Yaml();
		Iterable<Map<String, Object>> gitConfigs = cast(yaml.loadAll(gitConfigYaml.getContent()));
		for (Map<String, Object> configMap : gitConfigs) {
			if (configMap == null) {
				continue;
			}
			configMap.put("revision", gitConfigYaml.getRevision());
			gitHubConfig.add(objectMapper.convertValue(configMap, GitHubConfig.class));
		}
		return gitHubConfig;
	}

	/**
	 * get github configuration by name.
	 *
	 * @param user user.
	 * @param name configuration name.
	 * @return list of github configuration.
	 * @since 3.5.0
	 */
	public GitHubConfig getGitHubConfig(User user, String name) throws FileNotFoundException {
		List<GitHubConfig> gitHubConfigs = getAllGitHubConfig(user);
		Optional<GitHubConfig> gitHubConfigOptional = gitHubConfigs.stream()
			.filter(config -> StringUtils.equals(config.getName(), name))
			.findFirst();

		if (!gitHubConfigOptional.isPresent()) {
			throw new NGrinderRuntimeException("GitHub configuration(" + name + ") is not exist");
		}
		return gitHubConfigOptional.get();
	}

	public boolean validate(FileEntry gitConfigYaml) {
		for (GitHubConfig config : getAllGithubConfig(gitConfigYaml)) {
			try {
				getGitHubClient(config).getRepository(config.getOwner() + "/" + config.getRepo());
			} catch (IOException e) {
				Map<String, String> errorJson = deserialize(e.getMessage(), new TypeReference<Map<String, String>>() {});
				throw new NGrinderRuntimeException("Invalid git configuration.\n" + errorJson.get("message"));
			}
		}
		return true;
	}

	/**
	 * Get ngrinder test scripts from user github repository.
	 *
	 * @since 3.5.0
	 */
	@Cacheable(value = CACHE_GITHUB_SCRIPTS, key = "#user.userId")
	public Map<String, List<GHTreeEntry>> getScripts(User user) throws FileNotFoundException {
		Map<String, List<GHTreeEntry>> scriptMap = new HashMap<>();
		getAllGitHubConfig(user).forEach(gitHubConfig -> {
			try {
				GitHub gitHub = getGitHubClient(gitHubConfig);
				GHRepository ghRepository = gitHub.getRepository(gitHubConfig.getOwner() + "/" + gitHubConfig.getRepo());

				String defaultBranch = ghRepository.getDefaultBranch();
				String configuredBranch = gitHubConfig.getBranch();
				String activeBranch = defaultBranch;

				if (!isEmpty(configuredBranch)) {
					activeBranch = configuredBranch;
				}
				String shaOfDefaultBranch = ghRepository.getBranch(activeBranch).getSHA1();
				List<GHTreeEntry> allFiles = ghRepository.getTreeRecursive(shaOfDefaultBranch, 1).getTree();
				List<GHTreeEntry> scripts = filterScript(allFiles, removePrependedSlash(gitHubConfig.getScriptRoot()));

				if (scripts.size() > 0) {
					scripts.forEach(script -> script.setSha(createScriptRevisionUrl(ghRepository.getSvnUrl(), shaOfDefaultBranch, script.getPath())));
				}
				scriptMap.put(gitHubConfig.getName() + ":" + gitHubConfig.getRevision(), scripts);
			} catch (IOException e) {
				log.error("Fail to get script from github with [userId({}), {}]", user.getUserId(), gitHubConfig, e);
				throw new NGrinderRuntimeException("Fail to get script from github.\ncause: " + e.getCause(), e);
			}
		});
		return scriptMap;
	}

	private SVNURL createCheckoutUrl(GHRepository ghRepository,
									 String scriptPath,
									 String configuredBranch,
									 boolean isDefaultBranch) throws SVNException {
		String checkoutBaseUrl = ghRepository.getSvnUrl();
		checkoutBaseUrl += isDefaultBranch ? "/trunk" : "/branches/" + configuredBranch;
		SVNURL checkoutUrl;
		if (proxy(this).isGroovyMavenProject(ghRepository, scriptPath)) {
			checkoutUrl = parseURIEncoded(checkoutBaseUrl + "/" + scriptPath.split(MAVEN_PATH)[0]);
		} else {
			checkoutUrl = parseURIEncoded(checkoutBaseUrl + "/" + getFullPath(scriptPath));
		}
		return checkoutUrl;
	}

	/**
	 * Create GitHub client from {@link GitHubConfig}.
	 *
	 * @since 3.5.0
	 */
	public GitHub getGitHubClient(GitHubConfig gitHubConfig) {
		String baseUrl = getGitHubBaseUrl(gitHubConfig);
		String accessToken = gitHubConfig.getAccessToken();

		GitHubBuilder gitHubBuilder = new GitHubBuilder().withRateLimitHandler(rateLimitHandlerEx);

		if (isNotEmpty(baseUrl)) {
			gitHubBuilder.withEndpoint(baseUrl);
		}

		if (isNotEmpty(accessToken)) {
			gitHubBuilder.withOAuthToken(accessToken);
		}

		try {
			return gitHubBuilder.build();
		} catch (IOException e) {
			log.error("Fail to creation of github client from {}", gitHubConfig, e);
			Map<String, String> errorJson = deserialize(e.getMessage(), new TypeReference<Map<String, String>>() {});
			throw new NGrinderRuntimeException("Fail to creation of github client.\n" + errorJson.get("message"));
		}
	}

	private List<GHTreeEntry> filterScript(List<GHTreeEntry> ghTreeEntries, String scriptRoot) {
		return ghTreeEntries
			.stream()
			.filter(ghTreeEntry -> isScript(ghTreeEntry, scriptRoot))
			.collect(toList());
	}

	private boolean isScript(GHTreeEntry ghTreeEntry, String scriptRoot) {
		String path = ghTreeEntry.getPath();
		return ghTreeEntry.getType().endsWith("blob") && path.contains(scriptRoot)
			&& (path.endsWith(".groovy") || path.endsWith(".py"));
	}

	private String getGitHubBaseUrl(GitHubConfig gitHubConfig) {
		String configuredGitHubBaseUrl = gitHubConfig.getBaseUrl();
		return (!configuredGitHubBaseUrl.isEmpty()) ? configuredGitHubBaseUrl : config.getControllerProperties().getProperty(PROP_CONTROLLER_GITHUB_BASE_URL);
	}

	@CacheEvict(value = CACHE_GITHUB_SCRIPTS, key = "#user.userId")
	public void evictGitHubScriptCache(User user) {
		noOp();
	}

	@CacheEvict(value = CACHE_GITHUB_IS_MAVEN_GROOVY, key = "#scriptPath")
	public void evictGitHubMavenGroovyCache(String scriptPath) {
		noOp();
	}

	public static class RateLimitHandlerEx extends RateLimitHandler {
		@Override
		public void onError(IOException e, HttpURLConnection uc) {
			throw new NGrinderRuntimeException("GitHub api rate limit was hit.", e);
		}
	}
}
