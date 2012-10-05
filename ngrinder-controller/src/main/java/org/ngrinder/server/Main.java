package org.ngrinder.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * nGrinder controller startup class with embedded war (Jetty).
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public final class Main {

	private static final int DEFAULT_PORT = 80;
	private static final int DISPATCHER_SERVLET_INIT_ORDER = 3;
	private static final int MAX_FORM_CONTENT_SIZE = 512 * 1024 * 1024;

	/**
	 * Constructor.
	 */
	private Main() {
	}

	private void configure(Server server) {
		setConnector(server);
		setHandler(server);
	}

	private void setConnector(Server server) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(DEFAULT_PORT);
		server.addConnector(connector);
	}

	private void setHandler(Server server) {
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS) {
			@Override
			protected boolean isProtectedTarget(String target) {
				while (target.startsWith("//")) {
					target = URIUtil.compactPath(target);
				}

				return StringUtil.startsWithIgnoreCase(target, "/web-inf")
								|| StringUtil.startsWithIgnoreCase(target, "/meta-inf");
			}
		};
		handler.setContextPath("/");
		handler.setMaxFormContentSize(MAX_FORM_CONTENT_SIZE);
		addContextLoaderListener(handler);
		addDispatcherServlet(handler);

		addDefaultServlet(handler);
		server.setHandler(handler);
	}

	private void addContextLoaderListener(ServletContextHandler handler) {
		handler.addEventListener(new ContextLoaderListener());
		handler.setInitParameter("contextConfigLocation", "classpath:applicationContext.xml");
	}

	private void addDispatcherServlet(ServletContextHandler handler) {
		ServletHolder holder = new ServletHolder(new DispatcherServlet());
		holder.setInitParameter("contextConfigLocation", "classpath:servlet-context.xml");
		holder.setInitOrder(DISPATCHER_SERVLET_INIT_ORDER);
		handler.addServlet(holder, "/");
	}

	private void addDefaultServlet(ServletContextHandler handler) {
		ServletHolder holder = new ServletHolder(new DefaultServlet());
		// holder.setInitParameter("resourceBase", "");
		holder.setInitParameter("dirAllowed", "true");
		holder.setInitParameter("welcomeServlets", "false");
		holder.setInitParameter("gzip", "false");
		handler.addServlet(holder, "*.css");
		handler.addServlet(holder, "*.js");
		handler.addServlet(holder, "*.ico");
	}

	/**
	 * Main method for nGrinder startup.
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		try {
			Server server = new Server();
			new Main().configure(server);
			server.start();
			server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
