package com.nhncorp.ngrinder.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.nhncorp.ngrinder.NGrinderIocTestBase;
import com.nhncorp.ngrinder.bo.ManageBO;
import com.nhncorp.ngrinder.monitor.LogLoader;

public class ManageControllerTest extends NGrinderIocTestBase {
	private ManageController manageController;

	@Autowired
	private ManageBO manageBO;

	@Before
	public void initController() {
		if (manageController == null) {
			manageController = new ManageController();
			ReflectionTestUtils.setField(manageController, "manageBO", manageBO);
		}
	}

	@Test
	public void testManageController() throws InterruptedException {
		manageController.start();
		Thread.sleep(3000);
		assertTrue(manageController.isReady().contains("true"));
		manageController.stop();
		Thread.sleep(3000);
		assertFalse(manageController.isReady().contains("true"));
	}

	@Test
	public void testStartAlone() throws Exception {
		manageController.startAlone("localhost", 16372);
		Thread.sleep(3000);
		assertTrue(manageController.isReady().contains("true"));
		manageController.stop();
		Thread.sleep(3000);
		assertFalse(manageController.isReady().contains("true"));
	}
	
	@Test
	public void testReStart() throws Exception {
		manageController.reStart();
		Thread.sleep(3000);
		assertTrue(manageController.isReady().contains("true"));
		manageController.stop();
		Thread.sleep(3000);
		assertFalse(manageController.isReady().contains("true"));
	}

	@Test
	public void testLoadLogs() throws Exception {
		String root = this.getClass().getClassLoader().getResource(".").getPath();
		LogLoader.setTomcatRoot(root);

		ExecutorService service = Executors.newFixedThreadPool(4);
		Runnable server = new MockServer(6375);
		service.execute(server);
		Thread.sleep(1000);
		manageController.loadCatalinaLogs("localhost:6372");
		Thread.sleep(1000);
		LogLoader.stop("127.0.0.1", 6375, "logs/catalina.out");

		Runnable grinderServer = new MockServer(6376);
		service.execute(grinderServer);
		Thread.sleep(1000);
		manageController.loadGrinderLogs("localhost:6372", "logs/catalina.out");
		Thread.sleep(1000);
		LogLoader.stop("127.0.0.1", 6376, "logs/catalina.out");

		Thread.sleep(3000);
		Runnable grinderServer2 = new MockServer(6376);
		service.execute(grinderServer2);
		manageController.loadGrinderLogs("localhost-1:6372", "");
		Thread.sleep(1000);
		LogLoader.stop("127.0.0.1", 6376, "logs/catalina.out");
		Thread.sleep(1000);
	}
}

class MockServer implements Runnable {
	ServerSocket echoServer = null;
	String line;
	BufferedReader is;
	Socket clientSocket = null;
	private int port;

	MockServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			System.out.println("Mock server start.");
			echoServer = new ServerSocket(port);

			clientSocket = echoServer.accept();

			System.out.println("Client socket accepted from "
					+ ((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress().getHostAddress());
			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			while ((line = is.readLine()) != null) {
				System.out.println("Received: " + line);
			}

			System.out.println("Mock server stop");
			echoServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}