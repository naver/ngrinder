package org.ngrinder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.security.ProtectionDomain;

public class NGrinderControllerStarter {

	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder_app";
	@Parameter(names = "-port", description = "HTTP port of the server")
	private Integer port = 8080;
	@Parameter(names = "-context-path", description = "context path of the embedded web application")
	private String contextPath = "/";
	@Parameter(names = {"-help", "-?"}, description = "prints this message", hidden = true)
	private Boolean help = false;

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String defaultIfEmpty(String str, String defaultStr) {
		return isEmpty(str) ? defaultStr : str;
	}

	public File resolveHome() {
		String userHomeFromEnv = System.getenv("NGRINDER_APP");
		String userHomeFromProperty = System.getProperty("ngrinder.app");
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
		context.setTempDirectory(resolveHome());
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
		return protectionDomain.getCodeSource().getLocation().toExternalForm();
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

		if (getMaxPermGen() < (1024 * 1024 * 200)) {
			System.out.println("nGrinder needs quite big perm-gen memory." +
					" Please run with java -XX:MaxPermSize=200m -jar  " + new File(getWarName()).getName());
			System.exit(-1);
		}
		NGrinderControllerStarter server = new NGrinderControllerStarter();
		JCommander commander = new JCommander(server, args);
		if (server.help) {
			commander.usage();
		} else {
			server.run();
		}
	}

}