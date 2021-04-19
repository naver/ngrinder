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
package org.ngrinder.starter;

import com.beust.jcommander.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.ngrinder.common.constant.ClusterConstants.*;
import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_CONTROLLER_PORT;
import static org.ngrinder.common.constant.DatabaseConstants.PROP_DATABASE_TYPE;
import static org.ngrinder.common.constant.DatabaseConstants.PROP_DATABASE_URL;

@Slf4j
@SpringBootApplication
@ComponentScan(
	basePackages = {"org.ngrinder"},
	excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Controller.class)}
)
@Parameters(separators = "= ")
public class NGrinderControllerStarter extends SpringBootServletInitializer {

	@Parameters(separators = "= ")
	enum ClusterMode {
		none {
			@Parameter(names = {"-cp", "--controller-port"}, description = "controller port for agent connection",
				validateValueWith = PortAvailabilityValidator.class)
			private Integer controllerPort = 16001;

			public void process() {
				if (controllerPort != null) {
					System.setProperty(PROP_CONTROLLER_CONTROLLER_PORT, controllerPort.toString());
				}
			}
		},
		easy {
			@Parameter(names = {"-clh", "--cluster-host"},
				description = "This cluster member's cluster communication host. The default value is the " +
					"first non-localhost address. if it's localhost, " +
					"it can only communicate with the other cluster members in the same machine.")
			private String clusterHost;

			@Parameter(names = {"-clp", "--cluster-port"},
				description = "Deprecated from 3.5.4, the cluster communication port will be resolved automatically in easy clustering",
				validateValueWith = PortAvailabilityValidator.class)
			private Integer clusterPort;

			@Parameter(names = {"-cp", "--controller-port"}, required = true,
				description = "This cluster member's agent connection port",
				validateValueWith = PortAvailabilityValidator.class)
			private Integer controllerPort;

			@Parameter(names = {"-r", "--region"}, required = true,
				description = "This cluster member's region name")
			private String region;

			@Parameter(names = {"-sr", "--subregion"},
				description = "This cluster member's subregion names with ',' concatenated format like sub1,sub2")
			private String subregion = "";

			@Parameter(names = {"-dh", "--database-host"},
				description = "database host. The default value is localhost")
			private String databaseHost = "localhost";

			@Parameter(names = {"-dp", "--database-port"},
				description = "database port. The default value is 9092 when h2 is used and " +
					"3306 when mysql is used."
			)
			private Integer databasePort;

			@Parameter(names = {"-dt", "--database-type"},
				description = "database type", hidden = true)
			private String databaseType = "h2";

			@SuppressWarnings("SpellCheckingInspection")
			public void process() {
				System.setProperty(PROP_CLUSTER_MODE, "easy");
				if (isNotEmpty(clusterHost)) {
					System.setProperty(PROP_CLUSTER_HOST, clusterHost);
				}
				System.setProperty(PROP_CLUSTER_REGION, region);
				System.setProperty(PROP_CLUSTER_SUBREGION, subregion);
				System.setProperty(PROP_CONTROLLER_CONTROLLER_PORT, controllerPort.toString());
				System.setProperty(PROP_DATABASE_TYPE, databaseType);
				if ("h2".equals(databaseType)) {
					databasePort = defaultIfNull(databasePort, 9092);
					if (!checkConnection(databaseHost, databasePort)) {
						throw new ParameterException("Failed to connect h2 db " + databaseHost + ":" + databasePort
							+ ".\nPlease run the h2 TcpServer in advance\n"
							+ "or set the correct -database-host and -database-port parameters");
					}
					System.setProperty(PROP_DATABASE_URL, "tcp://" + databaseHost + ":" + databasePort + "/~/db/ngrinder");
				} else {
					databasePort = defaultIfNull(databasePort, 3306);
					if (!checkConnection(databaseHost, databasePort)) {
						throw new ParameterException("Failed to connect mysql db.\n" +
							"Please run the mysql db " + databaseHost + ":" + databasePort + "in advance\n" +
							"or set the correct -database-host and -database-port parameters");
					}
					System.setProperty(PROP_DATABASE_URL, databaseHost + ":" + databasePort);
				}
			}

		},
		advanced {
			public void process() {
				System.setProperty(PROP_CLUSTER_MODE, "advanced");
			}
		};

		public void parseArgs(String[] args) {
			JCommander commander = new JCommander(ClusterMode.this);
			String clusterModeOption = "";
			if (this != ClusterMode.none) {
				clusterModeOption = " --cluster-mode=" + name();
			}
			commander.setProgramName(getRunningCommand() + clusterModeOption);
			try {
				commander.parse(args);
				process();
			} catch (Exception e) {
				System.err.println("[Configuration Error]");
				System.err.println(e.getMessage());
				commander.usage();
				System.exit(-1);
			}
		}

		abstract void process();
	}

	private static boolean checkConnection(String byConnecting, int port) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(byConnecting, port), 2000); // 2 seconds timeout
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Parameter(names = {"-p", "--port"}, description = "HTTP port of the server. The default port is 8080.",
		validateValueWith = PortAvailabilityValidator.class)
	private Integer port;

	@Parameter(names = {"-c", "--context-path"}, description = "context path of the embedded web application.")
	private String contextPath = "/";

	@Parameter(names = {"-cm", "--cluster-mode"}, description = "cluster-mode can be easy or advanced  ")
	private String clusterMode = "none";

	@Parameter(names = {"-nh", "--ngrinder-home"}, description = "nGridner home directory")
	private String home;

	@SuppressWarnings("SpellCheckingInspection")
	@Parameter(names = {"-exh", "--ex-home"}, description = "nGridner extended home directory")
	private String exHome;

	@Parameter(names = {"-help", "-?", "-h"}, description = "prints this message")
	private Boolean help = false;


	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@DynamicParameter(names = "-D", description = "Dynamic parameters", hidden = true)
	private Map<String, String> params = new HashMap<>();

	private static String getWarName() {
		ProtectionDomain protectionDomain = NGrinderControllerStarter.class.getProtectionDomain();
		String warName = protectionDomain.getCodeSource().getLocation().toExternalForm();
		if (warName.endsWith("/classes/")) {
			warName = "ngrinder-controller-X.X.war";
		}
		return warName;
	}

	public static void main(String[] args) {
		NGrinderControllerStarter server = new NGrinderControllerStarter();
		JCommander commander = new JCommander(server);
		commander.setAcceptUnknownOptions(true);
		commander.setProgramName("ngrinder");
		PortAvailabilityValidator validator = new PortAvailabilityValidator();
		try {
			commander.parse(args);
			server.port = defaultIfNull(server.port, 8080);
			validator.validate("-p / --port", server.port);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			commander.usage();
			System.exit(0);
		}
		System.setProperty("server.port", Integer.toString(server.port));

		if (!server.contextPath.startsWith("/")) {
			server.contextPath = "/" + server.contextPath;
		}
		System.setProperty("server.servlet.context-path", server.contextPath);

		if (server.help) {
			commander.usage();
			System.exit(0);
		}

		if (isNotEmpty(server.home)) {
			System.setProperty("ngrinder.home", server.home);
		}
		if (isNotEmpty(server.exHome)) {
			System.setProperty("ngrinder.ex.home", server.exHome);
		}
		final List<String> unknownOptions = commander.getUnknownOptions();
		final ClusterMode clusterMode = ClusterMode.valueOf(server.clusterMode);
		clusterMode.parseArgs(unknownOptions.toArray(new String[0]));
		System.getProperties().putAll(server.params);
		cleanupPreviouslyUnpackedFolders();
		SpringApplication.run(NGrinderControllerStarter.class, args);
	}

	private static String getRunningCommand() {
		return "java -jar " + new File(getWarName()).getName();
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(NGrinderControllerStarter.class);
	}

	private static void cleanupPreviouslyUnpackedFolders() {
		File[] previouslyUnpackedFolder = FileUtils.getTempDirectory()
			.listFiles((dir, name) -> name.startsWith("ngrinder-controller") && !Config.getCurrentLibPath().contains(name));

		for (File file : Objects.requireNonNull(previouslyUnpackedFolder)) {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				log.error("Previously unpacked folder {} delete failed", file.getName(), e);
			}
		}
	}

}
