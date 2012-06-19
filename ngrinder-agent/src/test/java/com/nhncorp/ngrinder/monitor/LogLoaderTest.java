package com.nhncorp.ngrinder.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.nhncorp.ngrinder.NGrinderIocTestBase;

public class LogLoaderTest extends NGrinderIocTestBase {

    static int mockControllerPort = 9999;
    static String mockControllerHost = "localhost";

    @Test
    public void testRun() throws IOException, InterruptedException {
        MockServer server = new MockServer();
        String filePath = LogLoaderTest.class.getClassLoader().getResource("catalina.out").getPath();
        System.out.println("File path: " + filePath);
        LogLoader logLoader = new LogLoader(mockControllerHost, mockControllerPort, filePath, "localhost");

        ExecutorService service = Executors.newFixedThreadPool(4);
        service.execute(server);
        while (!server.isRunning) {
            Thread.sleep(100);
        }
        service.execute(logLoader);
        service.execute(logLoader);
        //        logLoader.run();
        
        Thread.sleep(1000);
        System.out.println("server.isRunning: " + server.isRunning);

        logLoader.stop();
        while (server.isRunning) {
            Thread.sleep(100);
        }

        String userRoot = System.getProperty("user.dir");
        if (userRoot.contains("\\")) {
            filePath = filePath.replaceAll("/", "\\\\");
        }
        System.out.println("user root: " + userRoot);
        if (filePath.contains(userRoot)) {
            LogLoader.setTomcatRoot(userRoot);
            filePath = filePath.substring(filePath.indexOf(userRoot) + userRoot.length() + 1);
            logLoader = new LogLoader(mockControllerHost, mockControllerPort, filePath, "localhost");

            service.execute(server);
            while (!server.isRunning) {
                Thread.sleep(100);
            }
            service.execute(logLoader);
            //        logLoader.run();
            Thread.sleep(1000);

            LogLoader.stop(mockControllerHost, mockControllerPort, filePath);
            while (server.isRunning) {
                Thread.sleep(100);
            }
        }

        Thread.sleep(1000);
        System.out.println("server.isRunning: " + server.isRunning);

        filePath += "noexist";
        logLoader = new LogLoader(mockControllerHost, mockControllerPort, filePath, "localhost");
        service.execute(server);
        while (!server.isRunning) {
            Thread.sleep(100);
        }
        service.execute(logLoader);
        //Thread.sleep(1000);
        logLoader.stop();
        while (server.isRunning) {
            Thread.sleep(100);
        }
    }

}

class MockServer implements Runnable {
    ServerSocket echoServer = null;
    String line;
    BufferedReader is;
    Socket clientSocket = null;
    
    boolean isRunning = false;

    @Override
    public void run() {
        try {
            System.out.println("Mock server start.");
            isRunning = true;
            echoServer = new ServerSocket(LogLoaderTest.mockControllerPort);

            clientSocket = echoServer.accept();
            //this.notify();
            System.out.println("Client socket accepted from "
                + ((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getAddress().getHostAddress());
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while ((line = is.readLine()) != null) {
                System.out.println("Received: " + line);
            }
            
            isRunning = false;
            System.out.println("Mock server stop");
            echoServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}