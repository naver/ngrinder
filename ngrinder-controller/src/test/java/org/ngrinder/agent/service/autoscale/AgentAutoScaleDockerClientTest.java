package org.ngrinder.agent.service.autoscale;

import com.beust.jcommander.internal.Lists;
import com.spotify.docker.client.messages.ContainerInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dasein.cloud.network.RawAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.model.Home;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by junoyoon on 15. 8. 18.
 * <p/>
 * Modified by shihuc 2015-08-19
 */
public class AgentAutoScaleDockerClientTest {


	private static final Logger LOG = LoggerFactory.getLogger(AgentAutoScaleDockerClientTest.class);

	Config config = mock(Config.class);

	AgentAutoScaleDockerClient dockerClient;

	@Before
	public void init() {
		when(config.getAgentAutoScaleControllerIP()).thenReturn("176.34.4.181");
		when(config.getAgentAutoScaleControllerPort()).thenReturn("8080");
		when(config.getAgentAutoScaleControllerIP()).thenReturn("11.11.11.11");
		when(config.getAgentAutoScaleControllerPort()).thenReturn("80");
		when(config.getAgentAutoScaleDockerRepo()).thenReturn("ngrinder/agent");
		when(config.getAgentAutoScaleDockerTag()).thenReturn("3.3-p1");
		dockerClient = new AgentAutoScaleDockerClient(config, "hello", Lists.newArrayList("127.0.0.1"));
		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));
	}

	@After
	public void clear() {
		IOUtils.closeQuietly(dockerClient);
	}

	@Test
	public void testDockerImageDownload() {
		dockerClient.createAndStartContainer("wow2");
	}


	@Test
	public void testCreateContainer1() {
		String containerName = "HelloUT";

		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));

		dockerClient.createContainer(containerName);
		String containerId = dockerClient.convertNameToId(containerName);

		assumeTrue(containerId != null);
		dockerClient.removeContainer(containerName);
		LOG.info("createContainer (try branch) is test...");
	}

	@Test
	public void testCreateContainer2() {

		String containerName = "HelloUT";

		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));

		dockerClient.createContainer(containerName);
		String containerId = dockerClient.convertNameToId(containerName);
		assumeTrue(containerId != null);
		dockerClient.startContainer(containerName);

		dockerClient.createContainer(containerName);
		dockerClient.removeContainer(containerName);

		String containerId2 = dockerClient.convertNameToId(containerName);
		assertTrue(containerId2 == null);
		LOG.info("createContainer (catch branch) is test...");
	}

	@Test
	public void testCreateAndStartContainer() {
		String containerName = "HelloUT";

		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));

		dockerClient.createAndStartContainer(containerName);
		String containerId = dockerClient.convertNameToId(containerName);
		assumeTrue(containerId != null);

		dockerClient.stopContainer(containerName);
		dockerClient.removeContainer(containerName);

		String containerId2 = dockerClient.convertNameToId(containerName);
		assertTrue(containerId2 == null);
		LOG.info("createAndStartContainer is test...");
	}

	@Test
	public void stopContainerTest() {
		String containerName = "HelloUT";

		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));

		dockerClient.createContainer(containerName);
		dockerClient.startContainer(containerName);
		dockerClient.stopContainer(containerName);
		dockerClient.removeContainer(containerName);

		ContainerInfo ci = dockerClient.inspectContainer(containerName);
		assertTrue(ci == null);
		LOG.info("stopContainer is test...");
	}

	@Test
	public void testStartContainer() {
		String containerName = "HelloUT";

		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));

		dockerClient.createContainer(containerName);
		dockerClient.startContainer(containerName);

		ContainerInfo ci = dockerClient.inspectContainer(containerName);
		assumeTrue(ci != null);
		assertTrue(ci.state().running());

		dockerClient.stopContainer(containerName);
		dockerClient.removeContainer(containerName);

		LOG.info("startContainer is test...");
	}

	@Test
	public void testWaitUtilContainerIsOn() {
		String containerName = "HelloUT";

		assumeTrue("OK".equalsIgnoreCase(dockerClient.ping()));

		dockerClient.createContainer(containerName);
		dockerClient.startContainer(containerName);
		dockerClient.waitUtilContainerIsOn(containerName);

		ContainerInfo ci = dockerClient.inspectContainer(containerName);
		assumeTrue(ci != null);
		assertTrue(ci.state().running());

		dockerClient.stopContainer(containerName);
		dockerClient.removeContainer(containerName);

		LOG.info("startContainer is test...");
	}
}
