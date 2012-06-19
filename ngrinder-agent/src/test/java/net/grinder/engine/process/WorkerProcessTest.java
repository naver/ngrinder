package net.grinder.engine.process;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SimpleSocketServer;

import com.nhncorp.ngrinder.util.HudsonPluginConfig;

public class WorkerProcessTest {

    static int testControllerPort = 6372;
    static String testControllerHost = "localhost";
    
    @Test
	public void testStartWorkerProcessForHudson() {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		SimpleSocketServer server = new SimpleSocketServer(lc, testControllerPort - 1);
		server.start();
		
		HudsonPluginConfig.setHudsonHost(testControllerHost);
		HudsonPluginConfig.setHudsonPort(Integer.valueOf(testControllerPort - 1));
		startWorkerProcess(true);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    @Test
	public void testStartWorkerProcessForController() {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		SimpleSocketServer dataServer = new SimpleSocketServer(lc, testControllerPort + 1);
		dataServer.start(); //data log
		SimpleSocketServer logServer = new SimpleSocketServer(lc, testControllerPort + 2);
		logServer.start(); //log

		startWorkerProcess(false);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    private void startWorkerProcess(final boolean forHudson) {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (forHudson) {
					WorkerProcessEntryPoint.main(new String[]{testControllerHost,
							String.valueOf(testControllerPort)});
				} else {
					WorkerProcessEntryPoint.main(new String[]{});
				}
			}
		}).start();
    }
}


