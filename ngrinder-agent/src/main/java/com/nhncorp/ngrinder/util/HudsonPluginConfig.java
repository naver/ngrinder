package com.nhncorp.ngrinder.util;

/**
 * The Class HudsonPluginConfig.
 *
 * @author Guowei Sun
 */
public class HudsonPluginConfig {

    /** The need to hudson. */
    private static boolean needToHudson = false;
    
    /** The hudson host. */
    private static String hudsonHost;
    
    /** The hudson port. */
    private static int hudsonPort;

    /**
     * Checks if is need to hudson.
     *
     * @return true, if is need to hudson
     */
    public static boolean isNeedToHudson() {
        return needToHudson;
    }

    /**
     * Sets the need to hudson.
     *
     * @param needToHudson the new need to hudson
     */
    public static void setNeedToHudson(boolean needToHudson) {
        HudsonPluginConfig.needToHudson = needToHudson;
    }

    /**
     * Gets the hudson host.
     *
     * @return the hudson host
     */
    public static String getHudsonHost() {
        return hudsonHost;
    }

    /**
     * Sets the hudson host.
     *
     * @param hudsonHost the new hudson host
     */
    public static void setHudsonHost(String hudsonHost) {
        HudsonPluginConfig.hudsonHost = hudsonHost;
    }

    /**
     * Gets the hudson port.
     *
     * @return the hudson port
     */
    public static int getHudsonPort() {
        return hudsonPort;
    }

    /**
     * Sets the hudson port.
     *
     * @param hudsonPort the new hudson port
     */
    public static void setHudsonPort(int hudsonPort) {
        HudsonPluginConfig.hudsonPort = hudsonPort;
    }

}
