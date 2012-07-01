package org.ngrinder.util;

/**
 * The Class FileUtil.
 *
 * @author Yin Peng
 */
public final class FileUtil {
	
	/**
	 * Instantiates a new file util.
	 */
	private FileUtil() {
	}
	
	/**
	 * Get hosts file name by os name.
	 * @param osName os name
	 * @return hosts file name
	 */
	public static String getHostsFileName(String osName) {
		String fileName = null;
		
		if ("linux".equalsIgnoreCase(osName)) {
			fileName = "/etc/hosts";
		} else {
			fileName = "C:\\WINDOWS\\system32\\drivers\\etc\\hosts";
		}
		
		return fileName;
	}
}
