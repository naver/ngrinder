package org.ngrinder.agent.service.autoscale;

import com.spotify.docker.client.*;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.messages.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkTrue;
import static org.ngrinder.common.util.ThreadUtils.sleep;

/**
 * This class is used to control the docker daemon which locates in the created AWS VM. The agent is
 * is running in the docker container running in VM.
 *
 * @author shihuc
 * @version 8/14/15 v1
 */
public class AgentAutoScaleDockerClient implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(AgentAutoScaleDockerClient.class);

	private String serverIP;

	private DockerClient dockerClient;

	private static final long CONNECT_TIMEOUT_MILLIS = 1000;
	private static final long READ_TIMEOUT_MILLIS = 5 * 1000;

	private static final int DAEMON_TCP_PORT = 10000;

	/*
	 * The docker image repository which identifies which image to run agent
	 */
	private String image;
	private String controllerUrl;
	private String region;

	/**
	 * Constructor function, in this function to do docker client api initialization.
	 *
	 * @param config    used to specify which docker image will be used
	 * @param addresses the docker daemon addresses
	 */
	public AgentAutoScaleDockerClient(Config config, String machineName, List<String> addresses) {
		this.region = config.getRegion();
		this.image = config.getAgentAutoScaleDockerRepo() + ":" + config.getAgentAutoScaleDockerTag();
		controllerUrl = config.getAgentAutoScaleControllerIP() + ":" + config.getAgentAutoScaleControllerPort();
		checkTrue(!addresses.isEmpty(), "Address should contains more than 1 element");
		for (String each : addresses) {
			String daemonUri = "http://" + each + ":" + DAEMON_TCP_PORT;
			try {
				ProxyAwareDockerClient.Builder builder = ProxyAwareDockerClient.builder();
				if (StringUtils.isNotEmpty(config.getProxyHost()) && config.getProxyPort() != 0) {
					builder = builder
							.proxyHost(config.getProxyHost())
							.proxyPort(config.getProxyPort());
				}
				dockerClient = builder
						.uri(daemonUri)
						.connectTimeoutMillis(CONNECT_TIMEOUT_MILLIS)
						.readTimeoutMillis(READ_TIMEOUT_MILLIS)
						.build();
				LOG.info("Try to connect {} docker using {}", machineName, daemonUri);
				return;
			} catch (Exception e) {
				// Fall through
				LOG.info("Access to {} using {} is failed", machineName, daemonUri);
			}
		}
		throw new NGrinderRuntimeException("No address for " + machineName + " can be connectible ");
	}

	/**
	 * This function is used to close the docker client api HTTP connection. Required to call after usage.
	 */
	@Override
	public void close() {
		IOUtils.closeQuietly(dockerClient);
	}

	/**
	 * This function is used to create and start one docker container.
	 * If the container exists, it skips the creation.
	 *
	 * @param containerId the container id or name which to start
	 */
	public void createAndStartContainer(String containerId) {
		createContainer(containerId);
		startContainer(containerId);
	}

	/**
	 * Stop the specified docker container.
	 *
	 * @param containerId the container id or name of which to be stopped
	 */
	public void stopContainer(String containerId) {
		try {
			dockerClient.stopContainer(containerId, 1);
			LOG.info("Stop docker container: {}", containerId);
		} catch (Exception e) {
			throw processException(e);
		}
	}

	/**
	 * Start the specified docker container.
	 *
	 * @param containerId the container id or name of which to be started
	 */
	protected void startContainer(String containerId) {
		try {
			dockerClient.startContainer(containerId);
		} catch (Exception e) {
			throw processException(e);
		}
	}

	/**
	 * Create the docker container with the given name.
	 *
	 * @param containerId the container id or name of which to be created
	 */
	protected void createContainer(String containerId) {
		LOG.info("Create docker container: {}", containerId);
		try {
			try {
				try {
					dockerClient.inspectImage(image);
				} catch (DockerException e) {
					LOG.info("Image " + image + " does not exist. Try to download.");
					dockerClient.pull(image, new ProgressHandler() {
						@Override
						public void progress(ProgressMessage message) throws DockerException {
							LOG.info("Image " + image + " is downloading {}", message.progressDetail());
						}
					});
				}

				/*
				 * Below function can wait some time until the http connection is ok before timeout
				 */
				final ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
				if (containerInfo.state().running()) {
					LOG.info("Container {} is already running, stop it", containerId);
					dockerClient.stopContainer(containerId, 0);
				}
			} catch (ContainerNotFoundException e) {
				ContainerConfig containerConfig = ContainerConfig.builder()
						.image(image)
						.hostConfig(HostConfig.builder().networkMode("host").build())
						.env("CONTROLLER_ADDR=" + controllerUrl)
						.env("REGION=" + region)
						.hostname(serverIP)
						.build();
				dockerClient.createContainer(containerConfig, containerId);
				LOG.info("Container {} is creating", containerId);
			}

		} catch (Exception e) {
			throw processException(e);
		}
	}

	public ContainerInfo waitUtilContainerIsOn(String containerId) {
		try {
			for (int i = 0; i < 5; i++) {
				try {
					final ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
					if (containerInfo.state().running()) {
						return containerInfo;
					}
					sleep(1000);
					if (i++ >= 5) {
						throw processException("container " + containerId + " is failed to run");
					}
				} catch (ContainerNotFoundException e) {
					throw processException("Container " + containerId + " is not found.", e);
				}
			}
		} catch (Exception e) {
			throw processException(e);
		}
		return null;
	}

	/**
	 * Convert the container name to the related container ID
	 *
	 * @param containerName container name
	 * @return String the container ID of the specified container name
	 */
	protected String convertNameToId(String containerName) {
		ListContainersParam listContainersParam = DockerClient.ListContainersParam.allContainers(true);
		String tempName = "/" + containerName;
		try {
			List<Container> containers = dockerClient.listContainers(listContainersParam);
			for (Container con : containers) {
				List<String> names = con.names();
				if (names != null && names.contains(tempName)) {
					return con.id();
				}
			}
		} catch (Exception e) {
			throw processException(e);
		}

		return null;
	}


	/**
	 * Unit Test purpose
	 */
	protected void removeContainer(String containerName) {
		try {
			dockerClient.removeContainer(containerName, true);
		} catch (Exception e) {
			throw processException(e);
		}
	}

	/**
	 * Unit Test purpose
	 */
	protected String ping() {
		try {
			return dockerClient.ping();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Unit Test purpose
	 */
	protected ContainerInfo inspectContainer(String containerName) {
		try {
			return dockerClient.inspectContainer(containerName);
		} catch (Exception e) {
			return null;
		}
	}
}
