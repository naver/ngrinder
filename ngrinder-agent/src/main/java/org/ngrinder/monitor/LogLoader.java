package org.ngrinder.monitor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will start a thread and load file content to send to specified host.<br/>
 * If a thread is already running to process the same file, no new thread will be started.
 * 
 */ 
public class LogLoader implements Runnable {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LogLoader.class);

    /** The controller port. */
    private final int controllerPort;

    /** The controller host. */
    private final String controllerHost;

    /** The log file path. */
    private final String logFilePath;

    /** The keep sending. */
    private boolean keepSending = false;

    private final String hostKey;

    private boolean running;

    private String message;

    /** The tomcat root. */
    private static String tomcatRoot = "";

    /** The max size to load. */
    private static int maxSizeToLoad = 4096;

    /** The Constant PROCESSING_FILE_SET. */
    private static final Map<String, LogLoader> LOG_LOADER_MAP = new ConcurrentHashMap<String, LogLoader>();

    private static final String COLON = ":";

    private static final String NEW_LINE = "\n";
    
    private File theFile;
    
    private String absolutePath;

    /**
     * Instantiates a new log loader.
     *
     * @param host the host
     * @param port the port
     * @param logFilePath the log file path
     * @param hostKey uni-key of agent host
     */
    public LogLoader(String host, int port, String logFilePath, String hostKey) {
        this.controllerHost = host;
        this.controllerPort = port;
        this.logFilePath = logFilePath;
        this.hostKey = hostKey;
        keepSending = true;
        absolutePath = getAbsolutePath();
        theFile = new File(absolutePath);
    }

    public void run() {
    
        running = true;
        String fileKey = controllerHost + COLON + controllerPort + COLON + logFilePath;

        if (LOG_LOADER_MAP.containsKey(fileKey)) {
            LOG.warn("======Log Load Error: the file:{} is currently sending to {}:{}",
            		new Object[]{logFilePath, controllerHost, controllerPort});
            return;
        }

        LOG_LOADER_MAP.put(fileKey, this);

        DataOutputStream output = null;
        RandomAccessFile raf = null;

        try {
            LOG.info("Starts to connect to controller server {}:{}", controllerHost, controllerPort);
            Socket logSocket = new Socket(controllerHost, controllerPort);
            output = new DataOutputStream(logSocket.getOutputStream());

            output.writeBytes(hostKey);
            output.writeBytes(NEW_LINE);
            output.flush();
            LOG.info("[Grinder-LogLoader]:Start to send log file:{}.{}", logFilePath,fileKey);
            raf = new RandomAccessFile(theFile, "r");
            String logLine;

            if (theFile.length() > maxSizeToLoad) {
                raf.seek(theFile.length() - maxSizeToLoad);
                raf.readLine();
            }

            long lastPosition = raf.getFilePointer();
            long lastAccessTime = System.currentTimeMillis();
            while (keepSending) {
                if (System.currentTimeMillis() - lastAccessTime > 1000 * 60) {
                    raf.close();
                    raf = new RandomAccessFile(theFile, "r");
                    raf.seek(lastPosition);
                }

                while ((logLine = raf.readLine()) != null) {

                    output.writeBytes(logLine);
                    output.writeBytes(NEW_LINE);
                    output.flush();
                }
                
                lastPosition = raf.getFilePointer();
                lastAccessTime = System.currentTimeMillis();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            logSocket.close();
            LOG.info("[Grinder-LogLoader]:Finished sending log file:{}.", absolutePath);
        } catch (IOException e) {
            LOG.error("[Grinder-LogLoader]:" + e.getMessage(), e);
            running = false;
            message = "[Grinder-LogLoader]:" + e.getMessage();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
            	LOG.error(e.getMessage(), e);
            }

            LOG_LOADER_MAP.remove(fileKey);
            running = false;
        }
    }
    
    public boolean checkLogFile(){
        if (!theFile.exists() || theFile.isDirectory()) {
            LOG.warn("[Grinder-LogLoader]:Can't read file from location:{}.", absolutePath);
            message = "[Grinder-LogLoader]:Can't read file from location " + absolutePath;
            return false;
        }
        return true;
    }

    public  String getAbsolutePath() {
        if (logFilePath.startsWith(File.separator)) {
            return logFilePath;
        }
        return tomcatRoot + File.separator + logFilePath;
    }
    
    /**
     * Sets the tomcat root.
     * @param root the new tomcat root
     */
    public static void setTomcatRoot(String root) {
        LOG.debug("Setting catalina.base as :{}", root);
        tomcatRoot = root;
    }

    /**
     * Stop trying to send any content.
     */
    public void stop() {
        LOG.info("stop loading file from:{} to {}:{}.", 
        		new Object[]{logFilePath, controllerHost, controllerPort});

        String fileKey = controllerHost + COLON + controllerPort + COLON + logFilePath;
        LOG_LOADER_MAP.remove(fileKey);

        keepSending = false;
        running = false;
    }

    /**
     * stop the specified logloader
     * @param controllerHost - web controller host
     * @param controllerPort - web controller port
     * @param logFilePath - log file path
     */
    public static void stop(String controllerHost, int controllerPort, String logFilePath) {
        String fileKey = controllerHost + COLON + controllerPort + COLON + logFilePath;

        if (LOG_LOADER_MAP.containsKey(fileKey)) {
            LOG.info("User stopped logLoader of file:{} to {}:{}.", 
            		new Object[]{logFilePath, controllerHost, controllerPort});

            LogLoader loader = LOG_LOADER_MAP.remove(fileKey);
            loader.stop();
        }
    }

    public boolean isRunning() {
        return running;
    }


    /**
     * get last error message
     * @return message
     */
    public String getMessage() {
        return message;
    }
}
