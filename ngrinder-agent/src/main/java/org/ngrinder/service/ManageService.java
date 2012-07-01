package org.ngrinder.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import org.ngrinder.manager.GrinderWrapper;
import org.ngrinder.monitor.LogLoader;
import org.ngrinder.util.HudsonPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * The Class ManageService.
 */
@Service
public class ManageService {
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ManageService.class);

    /**
     * The property file name
     */
    private static final String GRINDER_PROP_FILE = "/grinder.properties";

    /**
     * Start.
     * @param standAlone TODO
     * @throws Exception the exception
     */
    public void start() {
        HudsonPluginConfig.setNeedToHudson(false);
        GrinderWrapper.startAgent(false);
    }

    public void startAlone(String hudsonHost, int hudsonPort) {
    	if (isReady()) {
    		GrinderWrapper.stopAgent();
        }
    	HudsonPluginConfig.setHudsonHost(hudsonHost);
        HudsonPluginConfig.setHudsonPort(hudsonPort);
        HudsonPluginConfig.setNeedToHudson(true);

        GrinderWrapper.startAgent(true);
    }

    public void reStart() {
    	if (isReady()) {
    		GrinderWrapper.stopAgent();
        }
        HudsonPluginConfig.setNeedToHudson(false);
        GrinderWrapper.startAgent(false);
    }

    /**
     * Stop.
     *
     * @throws Exception the exception
     */
    public void stop() {
        GrinderWrapper.stopAgent();
    }

    /**
     * Checks if is ready.
     *
     * @return true, if is ready
     */
    public boolean isReady() {
        return GrinderWrapper.isRunning();
    }

    /**
     * Load catalina logs.
     * 
     * @param hostKey Key of agent host 
     * @throws URISyntaxException 
     * @return messages
     */
    public void loadCatalinaLogs(String hostKey) throws Exception {
        LOG.info("[ManageService]:Load catalina log from host:{} ", hostKey);

        String logPath = "logs/catalina.out";

        Properties properties = getPropFile();
        int consolePort = Integer.valueOf(properties.getProperty("grinder.consolePort", "6372"));
        int port = consolePort == 0xFFFF ? consolePort - 3 : consolePort + 3;

        String consoleHost = properties.getProperty("grinder.consoleHost", "127.0.0.1");
        LogLoader logLoader = new LogLoader(consoleHost, port, logPath, hostKey);
        if(!logLoader.checkLogFile()){
            throw new Exception(logLoader.getMessage());
        }
        
        //Executors.newSingleThreadExecutor().execute(logLoader);
        Thread loaderThread = new Thread(logLoader);
        loaderThread.setDaemon(true);
        loaderThread.start();

        Thread.sleep(200);

        if (!logLoader.isRunning()) {
            throw new Exception(logLoader.getMessage());
        }
    }

    /**
     * Load grinder logs
     * @param hostKey Key of agent host 
     * @param logPath Log file path
     * @return messages
     * @throws Exception 
     */
    public void loadGrinderLogs(String hostKey, String logPath) throws Exception {
        LOG.info("[ManageService]:Load Grinder log from host:{}.", hostKey);

        String grinderLogPath;

        if (logPath == null || logPath.trim().length() == 0) {
            grinderLogPath = "logs/grinder.log";
        } else {
            grinderLogPath = logPath;
        }

        Properties properties = getPropFile();
        int consolePort = Integer.valueOf(properties.getProperty("grinder.consolePort", "6372"));
        int port = consolePort == 0xFFFF ? consolePort - 4 : consolePort + 4;

        String consoleHost = properties.getProperty("grinder.consoleHost", "127.0.0.1");

        LogLoader logLoader = new LogLoader(consoleHost, port, grinderLogPath, hostKey);

        //Executors.newSingleThreadExecutor().execute(logLoader);
        Thread loaderThread = new Thread(logLoader);
        loaderThread.setDaemon(true);
        loaderThread.start();

        Thread.sleep(500);

        if (!logLoader.isRunning()) {
            throw new Exception(logLoader.getMessage());
        }
    }
    private Properties getPropFile() throws Exception {
        Properties prop = new Properties();
        File propFile = new File(GrinderWrapper.class.getResource(GRINDER_PROP_FILE).toURI());
        prop.load(new FileInputStream(propFile));

        return prop;
    }
}
