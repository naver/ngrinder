// Copyright (C) 2005, 2006 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.engine.process;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.CommunicationDefaults;
import net.grinder.communication.StreamReceiver;
import net.grinder.engine.messages.InitialiseGrinderMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.net.SocketAppender;

import com.nhncorp.ngrinder.util.HudsonPluginConfig;
import com.nhncorp.ngrinder.util.ReflectionUtil;

/**
 * Entry point for processes launched by the agent. It is re-written for nGrinder
 * usage.
 * 
 * @author Mavlarn
 */
public class WorkerProcessEntryPoint {

	/**
	 * Main method.
	 * 
	 * <p>
	 * This is not intended to be used directly; you should always start The
	 * Grinder by starting an agent process. If you're debugging, you might want
	 * to use the single threaded mode if you want the worker "process" to be
	 * launched in the same JVM as the agent. See the
	 * grinder.debug.singleprocess property.
	 * </p>
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(final String[] args) {
		System.out.println("Arguments: ");
		for (String string : args) {
			System.out.println(string);			
		}
		if (args.length > 1 && args.length != 2) {
			System.err.println("Usage: java " + GrinderProcess.class.getName());
			System.exit(-1);
		}

		if(args.length == 2){
			HudsonPluginConfig.setNeedToHudson(true);
	        HudsonPluginConfig.setHudsonHost(args[0]);
	        HudsonPluginConfig.setHudsonPort(Integer.valueOf(args[1]));
		}
		
		final int exitCode = new WorkerProcessEntryPoint().run(System.in);

		System.exit(exitCode);
	}

	/**
	 * Create and run a process.
	 * 
	 * @param agentCommunicationStream
	 *            The agent communication stream.
	 * @return Process exit code.
	 */
	public int run(InputStream agentCommunicationStream) {
		final GrinderProcess grinderProcess;
		final Logger logger = LoggerFactory.getLogger("worker-bootstrap");

		try {
			grinderProcess = new GrinderProcess(new StreamReceiver(
					agentCommunicationStream));
		} catch (Exception e) {
			System.err.println("Error initialising worker process ("
					+ e.getMessage() + ")");
			e.printStackTrace();
			return -2;
		}

		try {
        	
        	ch.qos.logback.classic.Logger outputWriter = (ch.qos.logback.classic.Logger)ReflectionUtil
					.getFieldValue(grinderProcess, "m_logger");
        	ch.qos.logback.classic.Logger dataWriter = (ch.qos.logback.classic.Logger)ReflectionUtil
					.getFieldValue(grinderProcess, "m_dataLogger");
        	
        	InitialiseGrinderMessage initMsg = (InitialiseGrinderMessage)ReflectionUtil
        			.getFieldValue(grinderProcess, "m_initialisationMessage");
        	int workerNum = initMsg.getWorkerIdentity().getNumber();
        	String grinderId = getHostName() + "-" + workerNum;
        	
        	SocketAppender logSA = null;
        	
        	if (HudsonPluginConfig.isNeedToHudson()) {
	        	logSA = new SocketAppender(HudsonPluginConfig.getHudsonHost(),
                		HudsonPluginConfig.getHudsonPort());
			} else {
				
				InitialiseGrinderMessage message = (InitialiseGrinderMessage) ReflectionUtil
						.getFieldValue(grinderProcess, "m_initialisationMessage");

	        	String consoleHost = message.getProperties().getProperty(
						GrinderProperties.CONSOLE_HOST,
						CommunicationDefaults.CONSOLE_HOST);
	        	int consolePort = message.getProperties().getInt(GrinderProperties.CONSOLE_PORT,
						CommunicationDefaults.CONSOLE_PORT);
	        	outputWriter.debug("Console Host:{}", consoleHost);
	        	outputWriter.debug("Console Port:{}", consolePort);
	        	
	        	logSA = new SocketAppender(consoleHost, consolePort + 2);
	        	SocketAppender dataSA = new SocketAppender(consoleHost, consolePort + 1);

	    		dataSA.setContext(outputWriter.getLoggerContext());	    	    
	    		dataSA.start();
		    	if (dataSA.isStarted()) {
		    		dataWriter.addAppender(dataSA);
		    	} else {
		    		outputWriter.warn("Log Server Host:{}:{} cannot be connected.",
		    				dataSA.getRemoteHost(), dataSA.getPort());
		    	}
	    		//data logger just use echo encoder, not to change to
	    		//info("Worker:[{}]", grinderId)
	    		dataWriter.info("Worker:[" + grinderId + "][" 
	    				+ InetAddress.getLocalHost().getHostAddress()+"]");
			}
        	
	    	logSA.setContext(outputWriter.getLoggerContext());	    	    
	    	logSA.start();
	    	if (logSA.isStarted()) {
	    		outputWriter.addAppender(logSA);
	    	} else {
	    		outputWriter.warn("Log Server Host:{}:{} cannot be connected.",
	    				logSA.getRemoteHost(), logSA.getPort());
	    	}
        	
            outputWriter.info("Worker:[{}][{}]", grinderId,
            		InetAddress.getLocalHost().getHostAddress()); 
			
			grinderProcess.run();
			return 0;
		} catch (Exception e) {
			logger.error("Error running worker process ({})", e.getMessage());
			return -3;
		} finally {
			grinderProcess.shutdown(agentCommunicationStream == System.in);
		}
	}
	
    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UNNAMED HOST";
        }
    }
}
