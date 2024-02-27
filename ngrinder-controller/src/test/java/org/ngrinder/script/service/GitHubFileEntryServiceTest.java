package org.ngrinder.script.service;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.GitHubConfig;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class GitHubFileEntryServiceTest extends AbstractNGrinderTransactionalTest {

    private static final String GITHUB_CONFIG_NAME = ".gitconfig.yml";

    @Autowired
    private GitHubFileEntryService gitHubFileEntryService;

    @Autowired
    private MockFileEntityRepository mockFileEntityRepository;

    @Before
    public void before() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), "repo");
        FileUtils.deleteQuietly(file);
        CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
        mockFileEntityRepository.setUserRepository(new File(file, getTestUser().getUserId()));
    }

    @Test
    public void getAllGitHubConfig() throws Exception {
        User testUser = getTestUser();

        FileEntry fileEntry = new FileEntry();
        fileEntry.setContent("name: My Github Config\n" +
                "owner: naver\n" +
                "repo: ngrinder\n" +
                "access-token: e1a47e652762b60a...3ddc0713b07g13k\n" +
                "---\n" +
                "name: Another Config\n" +
                "owner: naver\n" +
                "repo: pinpoint\n" +
                "access-token: t9a47e6ff262b60a...3dac0713b07e82a\n" +
                "branch: feature/add-ngrinder-scripts\n" +
                "base-url: https://api.mygithub.com\n" +
                "script-root: ngrinder-scripts\n"
        );
        fileEntry.setEncoding("UTF-8");
        fileEntry.setPath(GITHUB_CONFIG_NAME);
        fileEntry.setDescription("for unit test");
        mockFileEntityRepository.save(testUser, fileEntry, fileEntry.getEncoding());

        Set<GitHubConfig> gitHubConfigs = gitHubFileEntryService.getAllGitHubConfig(testUser);

        assertThat(gitHubConfigs.size(), is(2));
        assertThat(
                gitHubConfigs,
                hasItems(
                        GitHubConfig.builder()
                                .name("My Github Config")
                                .owner("naver")
                                .repo("ngrinder")
                                .accessToken("e1a47e652762b60a...3ddc0713b07g13k")
                                .build(),
                        GitHubConfig.builder()
                                .name("Another Config")
                                .owner("naver")
                                .repo("pinpoint")
                                .accessToken("t9a47e6ff262b60a...3dac0713b07e82a")
                                .branch("feature/add-ngrinder-scripts")
                                .baseUrl("https://api.mygithub.com")
                                .scriptRoot("ngrinder-scripts")
                                .build()
                )
        );
    }
}
