package org.ngrinder.script.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.GitConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GitService {

	private final FileEntryService fileEntryService;

	private final ObjectMapper objectMapper;

	@Value("classpath:gitconfig_template/gitconfig.yml")
	private Resource gitConfigTemplate;

	public List<GitConfig> getGitConfig(User user) {
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
}
