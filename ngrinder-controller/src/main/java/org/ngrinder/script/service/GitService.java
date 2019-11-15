package org.ngrinder.script.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.GitConfig;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.constant.CacheConstants.CACHE_GITHUB_SCRIPTS;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @since 3.5.0
 */
@Service
@RequiredArgsConstructor
public class GitService {

	private static final Logger LOGGER = getLogger(GitService.class);

	private final FileEntryService fileEntryService;

	private final ObjectMapper objectMapper;

	public List<GitConfig> getGitHubConfig(User user) {
		List<GitConfig> gitConfig = new ArrayList<>();
		FileEntry gitConfigYaml = fileEntryService.getOne(user, ".gitconfig.yml", -1L);
		if (gitConfigYaml == null) {
			return gitConfig;
		}

		// Yaml is not thread safe. so create it every time.
		Yaml yaml = new Yaml();
		Iterable<Object> gitConfigs = yaml.loadAll(gitConfigYaml.getContent());
		for (Object object : gitConfigs) {
			gitConfig.add(objectMapper.convertValue(object, GitConfig.class));
		}
		return gitConfig;
	}

	/**
	 * Get ngrinder test scripts from user github repository.
	 *
	 * @since 3.5.0
	 */
	@Cacheable(value = CACHE_GITHUB_SCRIPTS, key = "#user.userId")
	public List<String> getScripts(User user, GitConfig gitConfig) {
		String owner = gitConfig.getOwner();
		String repo = gitConfig.getRepo();

		if (isEmpty(gitConfig.getOwner()) || isEmpty(gitConfig.getRepo())) {
			LOGGER.error("Owner and repository configuration must not be empty. [userId({}), {}]", user.getUserId(), gitConfig);
			throw new NGrinderRuntimeException("Owner and repository configuration must not be empty.");
		}

		try {
			GitHub gitHub = getGitHubClient(gitConfig);
			GHRepository ghRepository = gitHub.getRepository(owner + "/" + repo);;
			String shaOfDefaultBranch = ghRepository.getBranch(ghRepository.getDefaultBranch()).getSHA1();
			List<GHTreeEntry> allFiles = ghRepository.getTreeRecursive(shaOfDefaultBranch, 1).getTree();
			return filterScript(allFiles);
		} catch (IOException e) {
			LOGGER.error("Fail to get script from git with [userId({}), {}]", user.getUserId(), gitConfig, e);
			throw new NGrinderRuntimeException("Fail to get script from git.");
		}
	}

	/**
	 * Create GitHub client from {@link GitConfig}.
	 *
	 * @since 3.5.0
	 */
	private GitHub getGitHubClient(GitConfig gitConfig) {
		String baseUrl = gitConfig.getBaseUrl();
		String accessToken = gitConfig.getAccessToken();

		GitHubBuilder gitHubBuilder = new GitHubBuilder();

		if (isNotEmpty(baseUrl)) {
			gitHubBuilder.withEndpoint(baseUrl);
		}

		if (isNotEmpty(accessToken)) {
			gitHubBuilder.withOAuthToken(accessToken);
		}

		try {
			return gitHubBuilder.build();
		} catch (IOException e) {
			LOGGER.error("Fail to creation of github client from {}", gitConfig, e);
			throw new NGrinderRuntimeException("Fail to creation of github client.");
		}
	}

	private List<String> filterScript(List<GHTreeEntry> ghTreeEntries) {
		return ghTreeEntries
			.stream()
			.filter(this::isScript)
			.map(GHTreeEntry::getPath)
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
}
