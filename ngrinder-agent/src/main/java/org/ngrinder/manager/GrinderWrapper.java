package org.ngrinder.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import net.grinder.common.GrinderException;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentDaemon;
import net.grinder.engine.agent.AgentImplementation;
import net.grinder.engine.agent.FileStoreUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the entry point of The Grinder agent process.
 *
 */
public final class GrinderWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(GrinderWrapper.class);
	private static final Logger logger = LoggerFactory.getLogger("agent");

    private static final Object SYNLOCK = new Object();

    //private static final String USE_CONSOLE = "grinder.useConsole";

    private static GrinderWrapper wrapper = null;

    private static Boolean running = false;

    private Agent agent;

    private static final String GRINDER_PROP_FILE = "/grinder.properties";

    public static void startAgent(final boolean standAlone) {
        synchronized (SYNLOCK) {
            if (running) {
                return;
            }
            running = true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wrapper = new GrinderWrapper(standAlone);
                    wrapper.agent.run();
                } catch (GrinderException e) {
                    running = false;
                    LOG.error(e.getMessage());
                }
            }
        }).start();
    }

    public static void stopAgent() {
        synchronized (SYNLOCK) {
        	if (running) {
    	        running = false;
    	        //Runtime.getRuntime().removeShutdownHook(((AgentDaemon)wrapper.agent).getShutdownHook());
    	        wrapper.agent.shutdown();
    	        AgentDaemon damon = (AgentDaemon)wrapper.agent;
    	        damon.waitForStop();
        	}
        }

    }

    private GrinderWrapper(boolean standAlone) {
        try {        	
        	File propertyFile = null;
			if (standAlone) {
				propertyFile = getFromFileStore();
				// before the properties is sent to agent, it is already set
				// stand alone.
				// makeSureStandAlone(propertyFile);
				LOG.info("Set to start stand alone!");
			} else {
                propertyFile = getFromResource();;
                //makeSureUseConsole(propertyFile);
                LOG.info("Set to start to use console!");                    
            }
            LOG.info("propertyFile:{}", propertyFile);

            agent = new AgentDaemon(logger, 10000, new AgentImplementation(logger, propertyFile, false));
        } catch (GrinderException e) {
            LOG.error("Initialize agent failed because of " + e.getMessage());
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public File getFromFileStore() throws UnknownHostException, FileNotFoundException {
//    	String fileStorePath = "./" + InetAddress.getLocalHost().getHostName() + "-file-store";
//        String currentPath = fileStorePath + File.separator + "current";
//        String propFilePath = currentPath + File.separator + "grinder.properties";
    	File propertyFile = FileStoreUtil.getFileStore();

        if (propertyFile != null && propertyFile.exists()) {
            return propertyFile;
        } else {
            throw new FileNotFoundException("File store not found:" + propertyFile.getAbsolutePath());
        }
    }
    
    private File getFromResource() throws URISyntaxException {
    	URL url = GrinderWrapper.class.getResource(GRINDER_PROP_FILE);
    	return new File(url.toURI());
    }

//    private void makeSureStandAlone(File propertyFile) {
//    	try {
//    	 	GrinderProperties properties = new GrinderProperties(propertyFile);
//	    	Boolean useConsole = properties.getBoolean(USE_CONSOLE, true);
//	    	if (useConsole) {
//	    		properties.setBoolean(USE_CONSOLE, false);
//	    		properties.save();
//	    	}
//		} catch (PersistenceException e) {
//            LOG.error(e.getMessage(), e);
//		}
//    }
//    
//    
//    private void makeSureUseConsole(File propertyFile) {
//    	try {
//    	 	GrinderProperties properties = new GrinderProperties(propertyFile);
//    		properties.remove(USE_CONSOLE);
//    		properties.save();
//		} catch (PersistenceException e) {
//            LOG.error(e.getMessage(), e);
//		}
//    }
    
    public static boolean isRunning() {
        return running;
    }

}
