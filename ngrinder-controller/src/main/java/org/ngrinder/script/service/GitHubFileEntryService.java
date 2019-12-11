package org.ngrinder.script.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.*;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.exception.PerfTestPrepareException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.GitHubConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static java.io.File.separator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.common.util.AopUtils.proxy;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.NoOp.noOp;
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

	private final HazelcastService hazelcastService;

	private static final String MAVEN_PATH = "/src/main/java";

	private static final RateLimitHandlerEx rateLimitHandlerEx = new RateLimitHandlerEx();

	public GitHubFileEntryService(FileEntryService fileEntryService, @Lazy ObjectMapper objectMapper,
								  Config config, HazelcastService hazelcastService) {
		this.fileEntryService = fileEntryService;
		this.objectMapper = objectMapper;
		this.config = config;
		this.hazelcastService = hazelcastService;
	}

	public FileEntry getOne(User user, GHRepository ghRepository, String gitHubconfigName, String scriptPath) {
		String fullPath = getCheckoutDirPath(user, ghRepository, gitHubconfigName, scriptPath);
		if (proxy(this).isGroovyMavenProject(ghRepository, scriptPath)) {
			fullPath += scriptPath.substring(scriptPath.indexOf(MAVEN_PATH));
			FileEntry fileEntry = createGitHubScriptFileEntry(fullPath);
			fileEntry.getProperties().put("type", "groovy-maven");
			fileEntry.getProperties().put("scriptPath", scriptPath);
			return fileEntry;
		} else {
			fullPath += scriptPath.substring(scriptPath.lastIndexOf("/"));
			return createGitHubScriptFileEntry(fullPath);
		}
	}

	public void checkoutGitHubScript(User user, GHRepository ghRepository, GitHubConfig gitHubConfig, String scriptPath) {
		try {
			String defaultBranch = ghRepository.getDefaultBranch();
			String sha = ghRepository.getBranch(defaultBranch).getSHA1();

			String checkoutDirPath = getCheckoutDirPath(user, ghRepository, gitHubConfig.getName(), scriptPath);
			SVNUpdateClient svnUpdateClient = newInstance().getUpdateClient();
			File checkoutDir = new File(checkoutDirPath);
			String checkoutBaseUrl = hazelcastService.get(CACHE_GITHUB_CHECKOUT_BASE_URL, gitHubConfig.getName());

			if (checkoutBaseUrl == null) {
				GHContent ghContent = ghRepository.getFileContent(scriptPath);
				URL url = new URL(ghContent.getHtmlUrl());
				checkoutBaseUrl = createCheckoutBaseUrl(gitHubConfig, url, defaultBranch);
				hazelcastService.put(CACHE_GITHUB_CHECKOUT_BASE_URL, gitHubConfig.getName(), checkoutBaseUrl);
			}

			SVNURL checkoutUrl;
			if (proxy(this).isGroovyMavenProject(ghRepository, scriptPath)) {
				checkoutUrl = parseURIEncoded(checkoutBaseUrl + "/" + scriptPath.split(MAVEN_PATH)[0]);
			} else {
				checkoutUrl = parseURIEncoded(checkoutBaseUrl + "/" + scriptPath.substring(0, scriptPath.lastIndexOf("/")));
			}

			if (!checkoutDir.exists()) {
				// TODO check export or checkout, update cost.
				svnUpdateClient.doExport(checkoutUrl, checkoutDir, HEAD, HEAD, "\n", true, INFINITY);
				// svnUpdateClient.doCheckout(checkoutUrl, checkoutDir, HEAD, HEAD, INFINITY, false);
				saveSha(sha, checkoutDirPath);
				log.info("github checkout to: {}, url: {} sha: {}", checkoutDir, checkoutUrl.toString(), sha);
			} else {
				if (!isSameRevision(sha, checkoutDirPath)) {
					deleteQuietly(checkoutDir);
					svnUpdateClient.doExport(checkoutUrl, checkoutDir, HEAD, HEAD, "\n", true, INFINITY);
					// svnUpdateClient.doUpdate(checkoutDir, HEAD, INFINITY, false, true);
					saveSha(sha, checkoutDirPath);
					log.info("github update to: {}, sha: {}", checkoutDir, sha);
				}
			}
		} catch (Exception e) {
			throw new PerfTestPrepareException("Failed to checkout scripts from github.\nPlease check your github configuration.", e);
		}
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

	private String getCheckoutDirPath(User user, GHRepository ghRepository, String gitHubconfigName, String scriptPath) {
		String checkoutScriptPath;
		if (proxy(this).isGroovyMavenProject(ghRepository, scriptPath)) {
			checkoutScriptPath = scriptPath.split(MAVEN_PATH)[0];
		} else {
			checkoutScriptPath = scriptPath.substring(0, scriptPath.lastIndexOf("/"));
		}
		return config.getHome().getUserRepoDirectory(user).getPath().replace(separator, "/")
			+ "/git/" + gitHubconfigName + "/" + checkoutScriptPath;
	}

	/**
	 * get all github configuration of {@link User}.
	 *
	 * @param user user.
	 * @return list of github configuration.
	 * @since 3.5.0
	 */
	public List<GitHubConfig> getAllGitHubConfig(User user) throws FileNotFoundException {
		List<GitHubConfig> gitHubConfig = new ArrayList<>();
		FileEntry gitConfigYaml = fileEntryService.getOne(user, GITHUB_CONFIG_NAME, -1L);
		if (gitConfigYaml == null) {
			throw new FileNotFoundException(GITHUB_CONFIG_NAME + " isn't exist.");
		}

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
				String shaOfDefaultBranch = ghRepository.getBranch(defaultBranch).getSHA1();
				List<GHTreeEntry> allFiles = ghRepository.getTreeRecursive(shaOfDefaultBranch, 1).getTree();
				List<GHTreeEntry> scripts = filterScript(allFiles);

				if (scripts.size() > 0) {
					GHContent ghContent = ghRepository.getFileContent(scripts.get(0).getPath());
					URL url = new URL(ghContent.getHtmlUrl());
					hazelcastService.put(CACHE_GITHUB_CHECKOUT_BASE_URL, gitHubConfig.getName()
						, createCheckoutBaseUrl(gitHubConfig, url, defaultBranch));
				}
				scriptMap.put(gitHubConfig.getName() + ":" + gitHubConfig.getRevision(), scripts);
			} catch (IOException e) {
				log.error("Fail to get script from github with [userId({}), {}]", user.getUserId(), gitHubConfig, e);
				throw new NGrinderRuntimeException("Fail to get script from github.\ncause: " + e.getCause() , e);
			}
		});
		return scriptMap;
	}

	private String createCheckoutBaseUrl(GitHubConfig gitHubConfig, URL htmlUrl, String defaultBranch) {
		return htmlUrl.getProtocol() + "://" + htmlUrl.getHost() + "/" + gitHubConfig.getOwner()
			+ "/" + gitHubConfig.getRepo() + "/branches/" + defaultBranch;
	}

	/**
	 * Create GitHub client from {@link GitHubConfig}.
	 *
	 * @since 3.5.0
	 */
	public GitHub getGitHubClient(GitHubConfig gitHubConfig) {
		String baseUrl = gitHubConfig.getBaseUrl();
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
			throw new NGrinderRuntimeException("Fail to creation of github client.\n" + e.getMessage());
		}
	}

	private List<GHTreeEntry> filterScript(List<GHTreeEntry> ghTreeEntries) {
		return ghTreeEntries
			.stream()
			.filter(this::isScript)
			.collect(toList());
	}

	private boolean isScript(GHTreeEntry ghTreeEntry) {
		String path = ghTreeEntry.getPath();
		return ghTreeEntry.getType().endsWith("blob")
			&& (path.endsWith(".groovy") || path.endsWith(".py"));
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
