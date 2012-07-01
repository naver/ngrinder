package org.ngrinder.monitor;

/**
 * The Class ControllerProperties.
 *
 * @author Guowei Sun
 */
public final class ControllerProperties {
    
    /** The console host. */
    private static String consoleHost = "127.0.0.1";

    /** The console port. */
    private static int consolePort = 6372;

    /**
     * The private constructor
     */
    private ControllerProperties(){
        // not allowed to instance
    }
    
    /**
     * Gets the console host.
     *
     * @return the console host
     */
    public static String getConsoleHost() {
        return consoleHost;
    }

    /**
     * Sets the console host.
     *
     * @param consoleHost the new console host
     */
    public static void setConsoleHost(String consoleHost) {
        ControllerProperties.consoleHost = consoleHost;
    }

    /**
     * Gets the console port.
     *
     * @return the console port
     */
    public static int getConsolePort() {
        return consolePort;
    }

    /**
     * Sets the console port.
     *
     * @param consolePort the new console port
     */
    public static void setConsolePort(int consolePort) {
        ControllerProperties.consolePort = consolePort;
    }

}
