package org.ngrinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Mock ngrinder console server. It just accept request and return true.
 * 
 * @author Mavlarn
 * 
 */
public class MockControllerServer {

	private volatile boolean running;

	public int consolePort = 6372;

	public MockControllerServer(int port) {
		this.consolePort = port;
	}

	public void start() {
		running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				startConsole();
			}
		}).start();
	}

	public void stop() {
		running = false;
	}

	private void startConsole() {
		running = true;
		ServerSocket agentSocket;
		try {
			agentSocket = new ServerSocket(consolePort);
			BufferedReader br = null;
			BufferedWriter writer = null;

			while (running) {
				Socket clientSocket = agentSocket.accept();

				try {
					br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						if (line.trim().length() == 0) {
							break;
						}
					}
					writer.write("HTTP/1.1 200 OK\r\n");
					writer.write("Content-Type: text/plain\r\n\r\n");
					writer.write("null");
					writer.flush();

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null) {
							br.close();
						}
						if (clientSocket != null) {
							clientSocket.close();
						}
						if (writer != null) {
							writer.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}