package org.ngrinder;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Parameters(separators = "=")
public class NGrinderControllerStarter {

	@Parameters(separators = "=")
	enum ClusterMode {
		none {
			@Parameter(names = "-controller-port", description = "agent connection port")
			public Integer controllerPort = null;

			public void process() {
				if (controllerPort != null) {
					System.setProperty("controller.controller_port", controllerPort.toString());
				}
			}
		},
		easy {
			@Parameter(names = "-cluster-port", required = false,
					description = "This cluster member's cluster communication port")
			private Integer clusterPort = null;

			@Parameter(names = "-controller-port", required = true,
					description = "This cluster member's agent connection port")
			private Integer controllerPort = null;

			@Parameter(names = "-region", required = true,
					description = "This cluster member's region name")
			private String region = null;


			@Parameter(names = "-database-host", required = false,
					description = "The database host. The default value is localhost")
			private String databaseHost = "localhost";

			@Parameter(names = "-database-port", required = false,
					description = "The H2 database Port. The default value is 9092")
			private Integer databasePort = 9092;

			@Parameter(names = "-database-type", required = false,
					description = "The database type. The default value is h2", hidden = true)
			private String databaseType = "h2";

			public void process() {
				System.setProperty("cluster.mode", "easy");
				System.setProperty("cluster.port", clusterPort.toString());
				System.setProperty("cluster.region", region);
				System.setProperty("controller.controller_port", controllerPort.toString());
				System.setProperty("database-type", databaseType);
				if ("h2".equals(databaseType)) {
					System.setProperty("database.url", "tcp://" + databaseHost + ":" + databasePort + "/db/ngrinder");
				} else {
					System.setProperty("database.url", "localhost:33000");
				}
			}

		},
		advanced {
			public void process() {
				System.setProperty("cluster.mode", "advanced");
			}
		};

		public void parseArgs(String[] args) {
			JCommander commander = new JCommander(ClusterMode.this);
			commander.setProgramName(getRunningCommand() + " -cluster-mode=" + name());
			try {
				commander.parse(args);
				process();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				commander.usage();
				System.exit(-1);
			}
		}

		abstract void process();
	}

	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder";
	@Parameter(names = "-port", description = "HTTP port of the server, The default is 8080")
	private Integer port = 8080;

	@Parameter(names = "-context-path", description = "context path of the embedded web application. The default is /")
	private String contextPath = "/";

	@Parameter(names = "-cluster-mode", description = "nGrinder cluster-mode can be easy or advanced  ")
	private String clusterMode = "none";

	@Parameter(names = "-home", description = "nGrinder home")
	private String home = null;

	@Parameter(names = {"-help", "-?"}, description = "prints this message", hidden = true)
	private Boolean help = false;

	@DynamicParameter(names = "-D", description = "Dynamic parameters")
	private Map<String, String> params = new HashMap<String, String>();


	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String defaultIfEmpty(String str, String defaultStr) {
		return isEmpty(str) ? defaultStr : str;
	}

	public File resolveHome() {
		String userHomeFromEnv = System.getenv("NGRINDER_HOME");
		String userHomeFromProperty = System.getProperty("ngrinder.home");
		String userHome = defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		return (!isEmpty(userHome)) ? new File(userHome) : new File(
				System.getProperty("user.home"), NGRINDER_DEFAULT_FOLDER);
	}


	private void run() {
		Server server = new Server();
		SocketConnector connector = new SocketConnector();
		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(port);
		server.setConnectors(new Connector[]{connector});

		WebAppContext context = new WebAppContext();
		final File home = resolveHome();
		home.mkdirs();
		context.setTempDirectory(home);
		context.setServer(server);
		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		context.setContextPath(contextPath);

		String war = getWarName();
		context.setWar(war);
		server.setHandler(context);
		try {
			server.start();
			//noinspection StatementWithEmptyBody
			while (System.in.read() != 'q') {
				// Fall through
			}
			server.stop();
			server.join();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private static String getWarName() {
		ProtectionDomain protectionDomain = NGrinderControllerStarter.class.getProtectionDomain();
		String warName = protectionDomain.getCodeSource().getLocation().toExternalForm();
		if (warName.endsWith("/classes/")) {
			warName = "ngrinder-controller-X.X.war";
		}
		return warName;
	}

	private static long getMaxPermGen() {
		for (MemoryPoolMXBean each : ManagementFactory.getMemoryPoolMXBeans()) {
			if (each.getName().endsWith("Perm Gen")) {
				return each.getUsage().getMax();
			}
		}
		return Long.MAX_VALUE;
	}

	public static void main(String[] args) throws Exception {
		if (System.getProperty("unit-test") == null && getMaxPermGen() < (1024 * 1024 * 200)) {
			System.out.println(
					"nGrinder needs quite big perm-gen memory.\n" +
							"Please run nGrinder with the following command.\n" +
							getRunningCommand());
			System.exit(-1);
		}
		NGrinderControllerStarter server = new NGrinderControllerStarter();
		JCommander commander = new JCommander(server);
		commander.setAcceptUnknownOptions(true);
		commander.setProgramName("ngrinder");
		commander.parse(args);

		if (server.help) {
			commander.usage();
		}

		if (server.home != null) {
			System.setProperty("ngrinder.home", server.home);
		}
		final List<String> unknownOptions = commander.getUnknownOptions();
		final ClusterMode clusterMode = ClusterMode.valueOf(server.clusterMode);
		clusterMode.parseArgs(unknownOptions.toArray(new String[unknownOptions.size()]));
		System.getProperties().putAll(server.params);
		server.run();
	}

	private static String getRunningCommand() {
		return "java -XX:MaxPermSize=200m -jar  " + new File(getWarName()).getName();
	}

}