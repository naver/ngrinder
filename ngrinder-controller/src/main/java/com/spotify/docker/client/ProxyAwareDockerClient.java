package com.spotify.docker.client;

/*
 * Copyright (c) 2014 Spotify AB.
 * Copyright (c) 2014 Oleg Poleshuk.
 * Copyright (c) 2014 CyDesign Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.io.CharStreams;
import com.google.common.net.HostAndPort;
import com.spotify.docker.client.messages.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.spotify.docker.client.ObjectMapperProvider.objectMapper;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.HttpMethod.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

/**
 * Docker Client which is aware of proxy host and port.
 */
public class ProxyAwareDockerClient implements DockerClient, Closeable {

	public static final String DEFAULT_UNIX_ENDPOINT = "unix:///var/run/docker.sock";
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 2375;

	private static final String UNIX_SCHEME = "unix";

	private static final Logger log = LoggerFactory.getLogger(DefaultDockerClient.class);

	public static final long NO_TIMEOUT = 0;

	private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = SECONDS.toMillis(5);
	private static final long DEFAULT_READ_TIMEOUT_MILLIS = SECONDS.toMillis(30);
	private static final int DEFAULT_CONNECTION_POOL_SIZE = 100;

	private static final ClientConfig DEFAULT_CONFIG = new ClientConfig(
			ObjectMapperProvider.class,
			JacksonFeature.class,
			LogsResponseReader.class,
			ProgressResponseReader.class);

	private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile("/?[a-zA-Z0-9_-]+");

	private static final GenericType<List<Container>> CONTAINER_LIST =
			new GenericType<List<Container>>() {
			};

	private static final GenericType<List<Image>> IMAGE_LIST =
			new GenericType<List<Image>>() {
			};

	private static final GenericType<List<ImageSearchResult>> IMAGES_SEARCH_RESULT_LIST =
			new GenericType<List<ImageSearchResult>>() {
			};

	private static final GenericType<List<RemovedImage>> REMOVED_IMAGE_LIST =
			new GenericType<List<RemovedImage>>() {
			};

	private final Client client;
	private final Client noTimeoutClient;

	private final URI uri;
	private final String apiVersion;
	private final AuthConfig authConfig;

	Client getClient() {
		return client;
	}

	Client getNoTimeoutClient() {
		return noTimeoutClient;
	}

	/**
	 * Create a new client with default configuration.
	 *
	 * @param uri The docker rest api uri.
	 */
	public ProxyAwareDockerClient(final String uri) {
		this(URI.create(uri.replaceAll("^unix:///", "unix://localhost/")));
	}

	/**
	 * Create a new client with default configuration.
	 *
	 * @param uri The docker rest api uri.
	 */
	public ProxyAwareDockerClient(final URI uri) {
		this(new Builder().uri(uri));
	}

	/**
	 * Create a new client with default configuration.
	 *
	 * @param uri                The docker rest api uri.
	 * @param dockerCertificates The certificates to use for HTTPS.
	 */
	public ProxyAwareDockerClient(final URI uri, final DockerCertificates dockerCertificates) {
		this(new Builder().uri(uri).dockerCertificates(dockerCertificates));
	}

	/**
	 * Create a new client using the configuration of the builder.
	 *
	 * @param builder DefaultDockerClient builder
	 */
	protected ProxyAwareDockerClient(final Builder builder) {
		URI originalUri = checkNotNull(builder.uri, "uri");
		this.apiVersion = builder.apiVersion();

		if ((builder.dockerCertificates != null) && !originalUri.getScheme().equals("https")) {
			throw new IllegalArgumentException(
					"An HTTPS URI for DOCKER_HOST must be provided to use Docker client certificates");
		}

		if (originalUri.getScheme().equals(UNIX_SCHEME)) {
			this.uri = UnixConnectionSocketFactory.sanitizeUri(originalUri);
		} else {
			this.uri = originalUri;
		}

		final PoolingHttpClientConnectionManager cm = getConnectionManager(builder);
		final PoolingHttpClientConnectionManager noTimeoutCm = getConnectionManager(builder);

		RequestConfig.Builder reqConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout((int) builder.connectTimeoutMillis)
				.setConnectTimeout((int) builder.connectTimeoutMillis)
				.setSocketTimeout((int) builder.readTimeoutMillis);
		if (builder.proxyHost != null && builder.proxyPort != 0) {
			reqConfigBuilder
					.setProxy(new HttpHost(builder.proxyHost, builder.proxyPort));
		}
		final RequestConfig requestConfig = reqConfigBuilder.build();

		final ClientConfig config = DEFAULT_CONFIG
				.connectorProvider(new ApacheConnectorProvider())
				.property(ApacheClientProperties.CONNECTION_MANAGER, cm)
				.property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);

		this.authConfig = builder.authConfig;

		this.client = ClientBuilder.newClient(config);

		// ApacheConnector doesn't respect per-request timeout settings.
		// Workaround: instead create a client with infinite read timeout,
		// and use it for waitContainer and stopContainer.
		final RequestConfig noReadTimeoutRequestConfig = RequestConfig.copy(requestConfig)
				.setSocketTimeout((int) NO_TIMEOUT)
				.build();
		this.noTimeoutClient = ClientBuilder.newBuilder()
				.withConfig(config)
				.property(ApacheClientProperties.CONNECTION_MANAGER, noTimeoutCm)
				.property(ApacheClientProperties.REQUEST_CONFIG, noReadTimeoutRequestConfig)
				.build();
	}

	public String getHost() {
		return fromNullable(uri.getHost()).or("localhost");
	}

	private PoolingHttpClientConnectionManager getConnectionManager(Builder builder) {
		final PoolingHttpClientConnectionManager cm =
				new PoolingHttpClientConnectionManager(getSchemeRegistry(builder));

		// Use all available connections instead of artificially limiting ourselves to 2 per server.
		cm.setMaxTotal(builder.connectionPoolSize);
		cm.setDefaultMaxPerRoute(cm.getMaxTotal());

		return cm;
	}

	private Registry<ConnectionSocketFactory> getSchemeRegistry(final Builder builder) {
		final SSLConnectionSocketFactory https;
		if (builder.dockerCertificates == null) {
			https = SSLConnectionSocketFactory.getSocketFactory();
		} else {
			https = new SSLConnectionSocketFactory(builder.dockerCertificates.sslContext(),
					builder.dockerCertificates.hostnameVerifier());
		}

		final RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
				.<ConnectionSocketFactory>create()
				.register("https", https)
				.register("http", PlainConnectionSocketFactory.getSocketFactory());

		if (builder.uri.getScheme().equals(UNIX_SCHEME)) {
			registryBuilder.register(UNIX_SCHEME, new UnixConnectionSocketFactory(builder.uri));
		}

		return registryBuilder.build();
	}

	@Override
	public void close() {
		client.close();
		noTimeoutClient.close();
	}

	@Override
	public String ping() throws DockerException, InterruptedException {
		final WebTarget resource = client.target(uri).path("_ping");
		return request(GET, String.class, resource, resource.request());
	}

	@Override
	public Version version() throws DockerException, InterruptedException {
		final WebTarget resource = resource().path("version");
		return request(GET, Version.class, resource, resource.request(APPLICATION_JSON_TYPE));
	}

	@Override
	public int auth(final AuthConfig authConfig) throws DockerException, InterruptedException {
		final WebTarget resource = resource().path("auth");
		final Response response =
				request(POST, Response.class, resource, resource.request(APPLICATION_JSON_TYPE),
						Entity.json(authConfig));
		return response.getStatus();
	}

	@Override
	public Info info() throws DockerException, InterruptedException {
		final WebTarget resource = resource().path("info");
		return request(GET, Info.class, resource, resource.request(APPLICATION_JSON_TYPE));
	}

	@Override
	public List<Container> listContainers(final ListContainersParam... params)
			throws DockerException, InterruptedException {
		WebTarget resource = resource()
				.path("containers").path("json");

		for (ListContainersParam param : params) {
			resource = resource.queryParam(param.name(), param.value());
		}

		return request(GET, CONTAINER_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
	}

	@Override
	public List<Image> listImages(ListImagesParam... params)
			throws DockerException, InterruptedException {
		WebTarget resource = resource()
				.path("images").path("json");

		final Map<String, String> filters = newHashMap();
		for (ListImagesParam param : params) {
			if (param instanceof ListImagesFilterParam) {
				filters.put(param.name(), param.value());
			} else {
				resource = resource.queryParam(param.name(), param.value());
			}
		}

		// If filters were specified, we must put them in a JSON object and pass them using the
		// 'filters' query param like this: filters={"dangling":["true"]}
		try {
			if (!filters.isEmpty()) {
				final StringWriter writer = new StringWriter();
				final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
				generator.writeStartObject();
				for (Map.Entry<String, String> entry : filters.entrySet()) {
					generator.writeArrayFieldStart(entry.getKey());
					generator.writeString(entry.getValue());
					generator.writeEndArray();
				}
				generator.writeEndObject();
				generator.close();
				// We must URL encode the string, otherwise Jersey chokes on the double-quotes in the json.
				final String encoded = URLEncoder.encode(writer.toString(), UTF_8.name());
				resource = resource.queryParam("filters", encoded);
			}
		} catch (IOException e) {
			throw new DockerException(e);
		}

		return request(GET, IMAGE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
	}

	@Override
	public ContainerCreation createContainer(final ContainerConfig config)
			throws DockerException, InterruptedException {
		return createContainer(config, null);
	}

	@Override
	public ContainerCreation createContainer(final ContainerConfig config, final String name)
			throws DockerException, InterruptedException {
		WebTarget resource = resource()
				.path("containers").path("create");

		if (name != null) {
			checkArgument(CONTAINER_NAME_PATTERN.matcher(name).matches(),
					"Invalid container name: \"%s\"", name);
			resource = resource.queryParam("name", name);
		}

		log.info("Creating container with ContainerConfig: {}", config);

		try {
			return request(POST, ContainerCreation.class, resource, resource
					.request(APPLICATION_JSON_TYPE), Entity.json(config));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ImageNotFoundException(config.image(), e);
				default:
					throw e;
			}
		}
	}

	@Override
	public void startContainer(final String containerId)
			throws DockerException, InterruptedException {
		checkNotNull(containerId, "containerId");

		log.info("Starting container with Id: {}", containerId);

		try {
			final WebTarget resource = resource()
					.path("containers").path(containerId).path("start");
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public void pauseContainer(final String containerId)
			throws DockerException, InterruptedException {
		checkNotNull(containerId, "containerId");

		try {
			final WebTarget resource = resource()
					.path("containers").path(containerId).path("pause");
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public void unpauseContainer(final String containerId)
			throws DockerException, InterruptedException {
		checkNotNull(containerId, "containerId");

		try {
			final WebTarget resource = resource()
					.path("containers").path(containerId).path("unpause");
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public void restartContainer(String containerId) throws DockerException, InterruptedException {
		restartContainer(containerId, 10);
	}

	@Override
	public void restartContainer(String containerId, int secondsToWaitBeforeRestart)
			throws DockerException, InterruptedException {
		checkNotNull(containerId, "containerId");
		checkNotNull(secondsToWaitBeforeRestart, "secondsToWait");
		try {
			final WebTarget resource = resource().path("containers").path(containerId)
					.path("restart")
					.queryParam("t", String.valueOf(secondsToWaitBeforeRestart));
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}


	@Override
	public void killContainer(final String containerId) throws DockerException, InterruptedException {
		try {
			final WebTarget resource = resource().path("containers").path(containerId).path("kill");
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public void stopContainer(final String containerId, final int secondsToWaitBeforeKilling)
			throws DockerException, InterruptedException {
		try {
			final WebTarget resource = noTimeoutResource()
					.path("containers").path(containerId).path("stop")
					.queryParam("t", String.valueOf(secondsToWaitBeforeKilling));
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 304: // already stopped, so we're cool
					return;
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public ContainerExit waitContainer(final String containerId)
			throws DockerException, InterruptedException {
		try {
			final WebTarget resource = noTimeoutResource()
					.path("containers").path(containerId).path("wait");
			// Wait forever
			return request(POST, ContainerExit.class, resource,
					resource.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public void removeContainer(final String containerId)
			throws DockerException, InterruptedException {
		removeContainer(containerId, false);
	}

	@Override
	public void removeContainer(final String containerId, final boolean removeVolumes)
			throws DockerException, InterruptedException {
		try {
			final WebTarget resource = resource()
					.path("containers").path(containerId);
			request(DELETE, resource, resource
					.queryParam("v", String.valueOf(removeVolumes))
					.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public InputStream exportContainer(String containerId)
			throws DockerException, InterruptedException {
		final WebTarget resource = resource()
				.path("containers").path(containerId).path("export");
		return request(GET, InputStream.class, resource,
				resource.request(APPLICATION_OCTET_STREAM_TYPE));
	}

	@Override
	public InputStream copyContainer(String containerId, String path)
			throws DockerException, InterruptedException {
		final WebTarget resource = resource()
				.path("containers").path(containerId).path("copy");

		// Internal JSON object; not worth it to create class for this
		JsonNodeFactory nf = JsonNodeFactory.instance;
		final JsonNode params = nf.objectNode().set("Resource", nf.textNode(path));

		return request(POST, InputStream.class, resource,
				resource.request(APPLICATION_OCTET_STREAM_TYPE),
				Entity.json(params));
	}

	@Override
	public ContainerInfo inspectContainer(final String containerId)
			throws DockerException, InterruptedException {
		try {
			final WebTarget resource = resource().path("containers").path(containerId).path("json");
			return request(GET, ContainerInfo.class, resource, resource.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public ContainerCreation commitContainer(final String containerId,
	                                         final String repo,
	                                         final String tag,
	                                         final ContainerConfig config,
	                                         final String comment,
	                                         final String author)
			throws DockerException, InterruptedException {

		checkNotNull(containerId, "containerId");
		checkNotNull(repo, "repo");
		checkNotNull(config, "containerConfig");

		WebTarget resource = resource()
				.path("commit")
				.queryParam("container", containerId)
				.queryParam("repo", repo)
				.queryParam("comment", comment);

		if (!isNullOrEmpty(author)) {
			resource = resource.queryParam("author", author);
		}
		if (!isNullOrEmpty(comment)) {
			resource = resource.queryParam("comment", comment);
		}
		if (!isNullOrEmpty(tag)) {
			resource = resource.queryParam("tag", tag);
		}

		log.info("Committing container id: {} to repository: {} with ContainerConfig: {}", new Object[]{containerId,
				repo, config});

		try {
			return request(POST, ContainerCreation.class, resource, resource
					.request(APPLICATION_JSON_TYPE), Entity.json(config));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public List<ImageSearchResult> searchImages(final String term)
			throws DockerException, InterruptedException {
		final WebTarget resource = resource().path("images").path("search")
				.queryParam("term", term);
		return request(GET, IMAGES_SEARCH_RESULT_LIST, resource,
				resource.request(APPLICATION_JSON_TYPE));
	}

	@Override
	public void pull(final String image) throws DockerException, InterruptedException {
		pull(image, new LoggingPullHandler(image));
	}

	@Override
	public void pull(final String image, final ProgressHandler handler)
			throws DockerException, InterruptedException {
		final ImageRef imageRef = new ImageRef(image);

		WebTarget resource = resource().path("images").path("create");

		resource = resource.queryParam("fromImage", imageRef.getImage());
		if (imageRef.getTag() != null) {
			resource = resource.queryParam("tag", imageRef.getTag());
		}
		ProgressStream pull = null;
		try {
			pull = request(POST, ProgressStream.class, resource,
					resource.request(APPLICATION_JSON_TYPE)
							.header("X-Registry-Auth", authHeader()));
			pull.tail(handler, POST, resource.getUri());
		} catch (Exception e) {
			throw new DockerException(e);
		} finally {
			IOUtils.closeQuietly(pull);
		}
	}

	@Override
	public void pull(final String image, final AuthConfig authConfig)
			throws DockerException, InterruptedException {
		pull(image, authConfig, new LoggingPullHandler(image));
	}

	@Override
	public void pull(final String image, final AuthConfig authConfig, final ProgressHandler handler)
			throws DockerException, InterruptedException {
		final ImageRef imageRef = new ImageRef(image);

		WebTarget resource = resource().path("images").path("create");

		resource = resource.queryParam("fromImage", imageRef.getImage());
		if (imageRef.getTag() != null) {
			resource = resource.queryParam("tag", imageRef.getTag());
		}
		ProgressStream pull = null;
		try {
			pull = request(POST, ProgressStream.class, resource,
					resource.request(APPLICATION_JSON_TYPE)
							.header("X-Registry-Auth", authHeader(authConfig)));
			pull.tail(handler, POST, resource.getUri());
		} catch (Exception e) {
			throw new DockerException(e);
		} finally {
			IOUtils.closeQuietly(pull);
		}
	}

	@Override
	public void push(final String image) throws DockerException, InterruptedException {
		push(image, new LoggingPushHandler(image));
	}

	@Override
	public void push(final String image, final ProgressHandler handler)
			throws DockerException, InterruptedException {
		final ImageRef imageRef = new ImageRef(image);

		WebTarget resource =
				resource().path("images").path(imageRef.getImage()).path("push");

		if (imageRef.getTag() != null) {
			resource = resource.queryParam("tag", imageRef.getTag());
		}

		// the docker daemon requires that the X-Registry-Auth header is specified
		// with a non-empty string even if your registry doesn't use authentication
		ProgressStream push = null;
		try {
			push =
					request(POST, ProgressStream.class, resource,
							resource.request(APPLICATION_JSON_TYPE)
									.header("X-Registry-Auth", authHeader()));
			push.tail(handler, POST, resource.getUri());
		} catch (Exception e) {
			throw new DockerException(e);
		} finally {
			IOUtils.closeQuietly(push);
		}
	}

	@Override
	public void tag(final String image, final String name)
			throws DockerException, InterruptedException {
		tag(image, name, false);
	}

	@Override
	public void tag(final String image, final String name, final boolean force)
			throws DockerException, InterruptedException {
		final ImageRef imageRef = new ImageRef(name);

		WebTarget resource = resource().path("images").path(image).path("tag");

		resource = resource.queryParam("repo", imageRef.getImage());
		if (imageRef.getTag() != null) {
			resource = resource.queryParam("tag", imageRef.getTag());
		}

		if (force) {
			resource = resource.queryParam("force", true);
		}

		try {
			request(POST, resource, resource.request());
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ImageNotFoundException(image, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public String build(final Path directory, final BuildParameter... params)
			throws DockerException, InterruptedException, IOException {
		return build(directory, null, new LoggingBuildHandler(), params);
	}

	@Override
	public String build(final Path directory, final String name, final BuildParameter... params)
			throws DockerException, InterruptedException, IOException {
		return build(directory, name, new LoggingBuildHandler(), params);
	}

	@Override
	public String build(final Path directory, final ProgressHandler handler,
	                    final BuildParameter... params)
			throws DockerException, InterruptedException, IOException {
		return build(directory, null, handler, params);
	}

	@Override
	public String build(final Path directory, final String name, final ProgressHandler handler,
	                    final BuildParameter... params)
			throws DockerException, InterruptedException, IOException {
		checkNotNull(handler, "handler");

		WebTarget resource = resource().path("build");

		for (final BuildParameter param : params) {
			resource = resource.queryParam(param.buildParamName, String.valueOf(param.buildParamValue));
		}
		if (name != null) {
			resource = resource.queryParam("t", name);
		}

		log.debug("Auth Config {}", authConfig);

		// Convert auth to X-Registry-Config format
		AuthRegistryConfig authRegistryConfig;
		if (authConfig == null) {
			authRegistryConfig = AuthRegistryConfig.EMPTY;
		} else {
			authRegistryConfig = new AuthRegistryConfig(authConfig.serverAddress(),
					authConfig.username(),
					authConfig.password(),
					authConfig.email(),
					authConfig.serverAddress());
		}
		CompressedDirectory compressedDirectory = null;
		InputStream fileStream = null;
		ProgressStream build = null;
		try {
			compressedDirectory = CompressedDirectory.create(directory);
			fileStream = Files.newInputStream(compressedDirectory.file());
			build =
					request(POST, ProgressStream.class, resource,
							resource.request(APPLICATION_JSON_TYPE)
									.header("X-Registry-Config",
											authRegistryHeader(authRegistryConfig)),
							Entity.entity(fileStream, "application/tar"));
			String imageId = null;
			while (build.hasNextMessage(POST, resource.getUri())) {
				final ProgressMessage message = build.nextMessage(POST, resource.getUri());
				final String id = message.buildImageId();
				if (id != null) {
					imageId = id;
				}
				handler.progress(message);
			}
			return imageId;
		} finally {
			IOUtils.closeQuietly(compressedDirectory);
			IOUtils.closeQuietly(fileStream);
			IOUtils.closeQuietly(build);
		}
	}

	@Override
	public String build(Path path, String s, String s1, ProgressHandler progressHandler, BuildParameter... buildParameters) throws DockerException, InterruptedException, IOException {
		return null;
	}

	@Override
	public ImageInfo inspectImage(final String image) throws DockerException, InterruptedException {
		try {
			final WebTarget resource = resource().path("images").path(image).path("json");
			return request(GET, ImageInfo.class, resource, resource.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ImageNotFoundException(image, e);
				default:
					throw e;
			}
		}
	}

	@Override
	public List<RemovedImage> removeImage(String image)
			throws DockerException, InterruptedException {
		return removeImage(image, false, false);
	}

	@Override
	public List<RemovedImage> removeImage(String image, boolean force, boolean noPrune)
			throws DockerException, InterruptedException {
		try {
			final WebTarget resource = resource().path("images").path(image)
					.queryParam("force", String.valueOf(force))
					.queryParam("noprune", String.valueOf(noPrune));
			return request(DELETE, REMOVED_IMAGE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ImageNotFoundException(image);
				default:
					throw e;
			}
		}
	}

	@Override
	public LogStream logs(final String containerId, final LogsParameter... params)
			throws DockerException, InterruptedException {
		WebTarget resource = resource()
				.path("containers").path(containerId).path("logs");

		for (final LogsParameter param : params) {
			resource = resource.queryParam(param.name().toLowerCase(Locale.ROOT), String.valueOf(true));
		}

		try {
			return request(GET, LogStream.class, resource,
					resource.request("application/vnd.docker.raw-stream"));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId);
				default:
					throw e;
			}
		}
	}

	@Override
	public LogStream attachContainer(final String containerId,
	                                 final AttachParameter... params) throws DockerException,
			InterruptedException {
		WebTarget resource = resource().path("containers").path(containerId)
				.path("attach");

		for (final AttachParameter param : params) {
			resource = resource.queryParam(param.name().toLowerCase(Locale.ROOT),
					String.valueOf(true));
		}

		try {
			return request(POST, LogStream.class, resource,
					resource.request("application/vnd.docker.raw-stream"));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId);
				default:
					throw e;
			}
		}
	}

	@Override
	public String execCreate(String containerId, String[] cmd, ExecParameter... params)
			throws DockerException, InterruptedException {
		WebTarget resource = resource().path("containers").path(containerId).path("exec");

		final StringWriter writer = new StringWriter();
		try {
			final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
			generator.writeStartObject();

			for (ExecParameter param : params) {
				generator.writeBooleanField(param.getName(), true);
			}

			generator.writeArrayFieldStart("Cmd");
			for (String s : cmd) {
				generator.writeString(s);
			}
			generator.writeEndArray();

			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			throw new DockerException(e);
		}


		String response;
		try {
			response = request(POST, String.class, resource,
					resource.request(APPLICATION_JSON_TYPE),
					Entity.json(writer.toString()));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId);
				default:
					throw e;
			}
		}

		try {
			JsonNode json = objectMapper().readTree(response);
			return json.findValue("Id").textValue();
		} catch (IOException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public LogStream execStart(String execId, ExecStartParameter... params)
			throws DockerException, InterruptedException {
		WebTarget resource = resource().path("exec").path(execId).path("start");

		final StringWriter writer = new StringWriter();
		try {
			final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
			generator.writeStartObject();

			for (ExecStartParameter param : params) {
				generator.writeBooleanField(param.getName(), true);
			}

			generator.writeEndObject();
			generator.close();
		} catch (IOException e) {
			throw new DockerException(e);
		}

		try {
			return request(POST, LogStream.class, resource,
					resource.request("application/vnd.docker.raw-stream"),
					Entity.json(writer.toString()));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ExecNotFoundException(execId);
				default:
					throw e;
			}
		}
	}

	@Override
	public ExecState execInspect(final String execId) throws DockerException, InterruptedException {
		WebTarget resource = resource().path("exec").path(execId).path("json");

		try {
			return request(GET, ExecState.class, resource, resource.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ExecNotFoundException(execId);
				default:
					throw e;
			}
		}
	}

	@Override
	public ContainerStats stats(final String containerId)
			throws DockerException, InterruptedException {
		final WebTarget resource = resource().path("containers").path(containerId).path("stats")
				.queryParam("stream", "0");

		try {
			return request(GET, ContainerStats.class, resource, resource.request(APPLICATION_JSON_TYPE));
		} catch (DockerRequestException e) {
			switch (e.status()) {
				case 404:
					throw new ContainerNotFoundException(containerId);
				default:
					throw e;
			}
		}
	}

	private WebTarget resource() {
		final WebTarget target = client.target(uri);
		if (!isNullOrEmpty(apiVersion)) {
			target.path(apiVersion);
		}
		return target;
	}

	private WebTarget noTimeoutResource() {
		final WebTarget target = noTimeoutClient.target(uri);
		if (!isNullOrEmpty(apiVersion)) {
			target.path(apiVersion);
		}
		return target;
	}

	private <T> T request(final String method, final GenericType<T> type,
	                      final WebTarget resource, final Invocation.Builder request)
			throws DockerException, InterruptedException {
		try {
			return request.async().method(method, type).get();
		} catch (ExecutionException e) {
			throw propagate(method, resource, e);
		} catch (MultiException e) {
			throw propagate(method, resource, e);
		}
	}

	private <T> T request(final String method, final Class<T> clazz,
	                      final WebTarget resource, final Invocation.Builder request)
			throws DockerException, InterruptedException {
		try {
			return request.async().method(method, clazz).get();
		} catch (ExecutionException e) {
			throw propagate(method, resource, e);
		} catch (MultiException e) {
			throw propagate(method, resource, e);
		}
	}

	private <T> T request(final String method, final Class<T> clazz,
	                      final WebTarget resource, final Invocation.Builder request,
	                      final Entity<?> entity)
			throws DockerException, InterruptedException {
		try {
			return request.async().method(method, entity, clazz).get();
		} catch (ExecutionException e) {
			throw propagate(method, resource, e);
		} catch (MultiException e) {
			throw propagate(method, resource, e);
		}
	}

	private void request(final String method,
	                     final WebTarget resource,
	                     final Invocation.Builder request)
			throws DockerException, InterruptedException {
		try {
			request.async().method(method, String.class).get();
		} catch (ExecutionException e) {
			throw propagate(method, resource, e);
		} catch (MultiException e) {
			throw propagate(method, resource, e);
		}
	}

	private RuntimeException propagate(final String method, final WebTarget resource,
	                                   final Exception e)
			throws DockerException, InterruptedException {
		Throwable cause = e.getCause();

		// Sometimes e is a org.glassfish.hk2.api.MultiException
		// which contains the cause we're actually interested in.
		// So we unpack it here.
		if (e instanceof MultiException) {
			cause = cause.getCause();
		}

		Response response = null;
		if (cause instanceof ResponseProcessingException) {
			response = ((ResponseProcessingException) cause).getResponse();
		} else if (cause instanceof WebApplicationException) {
			response = ((WebApplicationException) cause).getResponse();
		} else if ((cause instanceof ProcessingException) && (cause.getCause() != null)) {
			// For a ProcessingException, The exception message or nested Throwable cause SHOULD contain
			// additional information about the reason of the processing failure.
			cause = cause.getCause();
		}

		if (response != null) {
			throw new DockerRequestException(method, resource.getUri(), response.getStatus(),
					message(response), cause);
		} else if ((cause instanceof SocketTimeoutException) ||
				(cause instanceof ConnectTimeoutException)) {
			throw new DockerTimeoutException(method, resource.getUri(), e);
		} else if ((cause instanceof InterruptedIOException)
				|| (cause instanceof InterruptedException)) {
			throw new InterruptedException("Interrupted: " + method + " " + resource);
		} else {
			throw new DockerException(e);
		}
	}

	private String message(final Response response) {
		final Readable reader = new InputStreamReader(response.readEntity(InputStream.class), UTF_8);
		try {
			return CharStreams.toString(reader);
		} catch (IOException ignore) {
			return null;
		}
	}

	private String authHeader() throws DockerException {
		return authHeader(authConfig);
	}

	private String authHeader(final AuthConfig authConfig) throws DockerException {
		if (authConfig == null) {
			return "null";
		}
		try {
			return Base64.encodeAsString(ObjectMapperProvider
					.objectMapper()
					.writeValueAsString(authConfig));
		} catch (JsonProcessingException ex) {
			throw new DockerException("Could not encode X-Registry-Auth header", ex);
		}
	}

	private String authRegistryHeader(final AuthRegistryConfig authRegistryConfig)
			throws DockerException {
		if (authRegistryConfig == null) {
			return "null";
		}
		try {
			String authRegistryJson =
					ObjectMapperProvider.objectMapper().writeValueAsString(authRegistryConfig);
			log.debug("Registry Config Json {}", authRegistryJson);
			String authRegistryEncoded = Base64.encodeAsString(authRegistryJson);
			log.debug("Registry Config Encoded {}", authRegistryEncoded);
			return authRegistryEncoded;
		} catch (JsonProcessingException ex) {
			throw new DockerException("Could not encode X-Registry-Config header", ex);
		}
	}

	/**
	 * Create a new {@link DefaultDockerClient} builder.
	 *
	 * @return Returns a builder that can be used to further customize and then build the client.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a new {@link DefaultDockerClient} builder prepopulated with values loaded
	 * from the DOCKER_HOST and DOCKER_CERT_PATH environment variables.
	 *
	 * @return Returns a builder that can be used to further customize and then build the client.
	 * @throws DockerCertificateException if we could not build a DockerCertificates object
	 */
	public static Builder fromEnv() throws DockerCertificateException {
		final String endpoint = fromNullable(getenv("DOCKER_HOST")).or(defaultEndpoint());
		final String dockerCertPath = getenv("DOCKER_CERT_PATH");

		final Builder builder = new Builder();

		if (endpoint.startsWith(UNIX_SCHEME + "://")) {
			builder.uri(endpoint);
		} else {
			final String stripped = endpoint.replaceAll(".*://", "");
			final HostAndPort hostAndPort = HostAndPort.fromString(stripped);
			final String hostText = hostAndPort.getHostText();
			final String scheme = isNullOrEmpty(dockerCertPath) ? "http" : "https";

			final int port = hostAndPort.getPortOrDefault(DEFAULT_PORT);
			final String address = isNullOrEmpty(hostText) ? DEFAULT_HOST : hostText;

			builder.uri(scheme + "://" + address + ":" + port);
		}

		if (!isNullOrEmpty(dockerCertPath)) {
			builder.dockerCertificates(new DockerCertificates(Paths.get(dockerCertPath)));
		}

		return builder;
	}

	private static String defaultEndpoint() {
		if (getProperty("os.name").equalsIgnoreCase("linux")) {
			return DEFAULT_UNIX_ENDPOINT;
		} else {
			return DEFAULT_HOST + ":" + DEFAULT_PORT;
		}
	}

	public static class Builder {

		private URI uri;
		private String apiVersion;
		private long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
		private long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;
		private int connectionPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
		private DockerCertificates dockerCertificates;
		private AuthConfig authConfig;
		private String proxyHost;
		private int proxyPort;

		public URI uri() {
			return uri;
		}

		public Builder uri(final URI uri) {
			this.uri = uri;
			return this;
		}

		/**
		 * Set the URI for connections to Docker.
		 *
		 * @param uri URI String for connections to Docker
		 * @return Builder
		 */
		public Builder uri(final String uri) {
			return uri(URI.create(uri));
		}

		/**
		 * Set the Docker API version that will be used in the HTTP requests to Docker daemon.
		 *
		 * @param apiVersion String for Docker API version
		 * @return Builder
		 */
		public Builder apiVersion(final String apiVersion) {
			this.apiVersion = apiVersion;
			return this;
		}

		public String apiVersion() {
			return apiVersion;
		}

		public long connectTimeoutMillis() {
			return connectTimeoutMillis;
		}

		/**
		 * Set the timeout in milliseconds until a connection to Docker is established.
		 * A timeout value of zero is interpreted as an infinite timeout.
		 *
		 * @param connectTimeoutMillis connection timeout to Docker daemon in milliseconds
		 * @return Builder
		 */
		public Builder connectTimeoutMillis(final long connectTimeoutMillis) {
			this.connectTimeoutMillis = connectTimeoutMillis;
			return this;
		}

		public long readTimeoutMillis() {
			return readTimeoutMillis;
		}

		/**
		 * Set the SO_TIMEOUT in milliseconds. This is the maximum period of inactivity
		 * between receiving two consecutive data packets from Docker.
		 *
		 * @param readTimeoutMillis read timeout to Docker daemon in milliseconds
		 * @return Builder
		 */
		public Builder readTimeoutMillis(final long readTimeoutMillis) {
			this.readTimeoutMillis = readTimeoutMillis;
			return this;
		}

		public DockerCertificates dockerCertificates() {
			return dockerCertificates;
		}

		/**
		 * Provide certificates to secure the connection to Docker.
		 *
		 * @param dockerCertificates DockerCertificates object
		 * @return Builder
		 */
		public Builder dockerCertificates(final DockerCertificates dockerCertificates) {
			this.dockerCertificates = dockerCertificates;
			return this;
		}

		public int connectionPoolSize() {
			return connectionPoolSize;
		}

		/**
		 * Set the size of the connection pool for connections to Docker. Note that due to
		 * a known issue, DefaultDockerClient maintains two separate connection pools, each
		 * of which is capped at this size. Therefore, the maximum number of concurrent
		 * connections to Docker may be up to 2 * connectionPoolSize.
		 *
		 * @param connectionPoolSize connection pool size
		 * @return Builder
		 */
		public Builder connectionPoolSize(final int connectionPoolSize) {
			this.connectionPoolSize = connectionPoolSize;
			return this;
		}

		public AuthConfig authConfig() {
			return authConfig;
		}

		/**
		 * Set the auth parameters for pull/push requests from/to private repositories.
		 *
		 * @param authConfig AuthConfig object
		 * @return Builder
		 */
		public Builder authConfig(final AuthConfig authConfig) {
			this.authConfig = authConfig;
			return this;
		}

		public ProxyAwareDockerClient build() {
			return new ProxyAwareDockerClient(this);
		}

		public String proxyHost() {
			return this.proxyHost;
		}

		public Builder proxyHost(String proxyHost) {
			this.proxyHost = proxyHost;
			return this;
		}

		public int proxyPort() {
			return this.proxyPort;
		}

		public Builder proxyPort(int port) {
			this.proxyPort = port;
			return this;
		}
	}
}
