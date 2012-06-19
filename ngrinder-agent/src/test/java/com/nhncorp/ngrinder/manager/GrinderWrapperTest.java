package com.nhncorp.ngrinder.manager;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SimpleSocketServer;

import com.nhncorp.ngrinder.MockControllerServer;
import com.nhncorp.ngrinder.util.HudsonPluginConfig;

public class GrinderWrapperTest {

    static int testControllerPort = 6372;
    static String testControllerHost = "localhost";

    static int testHudsonPort = 12345;
    static String testHudsonHost = "localhost";
	
    @Test
	public void testStartAgent () throws IOException {

//		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//		SimpleSocketServer server1 = new SimpleSocketServer(lc, testControllerPort + 1);
//		server1.start();//data
//		SimpleSocketServer server2 = new SimpleSocketServer(lc, testControllerPort + 2);
//		server2.start();//log
    	MockControllerServer server = new MockControllerServer(testControllerPort);
    	server.start();
    	boolean standAlone = false;
        GrinderWrapper.startAgent(standAlone); //start use console
        
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GrinderWrapper.stopAgent();
		server.stop();
	}
	
    @Test
	public void testStartHudsonAgent () throws IOException {
    	
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		SimpleSocketServer server = new SimpleSocketServer(lc, GrinderWrapperTest.testHudsonPort);
		server.start();
		
    	HudsonPluginConfig.setHudsonHost(testHudsonHost);
        HudsonPluginConfig.setHudsonPort(testHudsonPort);
        HudsonPluginConfig.setNeedToHudson(true);

        GrinderWrapper.startAgent(true);
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

//class MockLogServer implements Runnable {
//    ServerSocket echoServer = null;
//    String line;
//    BufferedReader is;
//    Socket clientSocket = null;
//    int port;
//    
//    boolean isRunning = false;
//    
//    public MockLogServer (int port) {
//    	this.port = port;
//    }
//
//    @Override
//    public void run() {
//        try {
//            System.out.println("Mock Log server start.");
//            isRunning = true;
//            echoServer = new ServerSocket(port);
//
//            clientSocket = echoServer.accept();
//
//            System.err.println("Client socket accepted from "
//                + ((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getAddress().getHostAddress());
//            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//            while ((line = is.readLine()) != null) {
//                System.err.println(port + " Received: " + line);
//            }
//            
//            isRunning = false;
//            System.out.println("Mock Log server stop");
//            echoServer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
