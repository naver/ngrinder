/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.sm;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * nGrinder security manager.
 *
 * @author JunHo Yoon
 * @author Tobi
 * @since 3.0
 */
@SuppressWarnings("Duplicates")
public class NGrinderSecurityManager extends SecurityManager {
	private static final String NGRINDER_CONTROLLER_DEFAULT_FOLDER = ".ngrinder";
	private static final String NGRINDER_CONTROLLER_TEMP_FOLDER = "tmp";
	private static final String NGRINDER_CONTEXT_CONTROLLER = "controller";

	private String workDirectory = System.getProperty("user.dir");
	private String controllerHomeDir = "";
	private String controllerHomeTmpDir = "";
	private String ngrinderContext = "";

	private final String pythonPath = System.getProperty("python.path");
	private final String pythonHome = System.getProperty("python.home");
	private final String pythonCache = System.getProperty("python.cachedir");
	private final String etcHosts = System.getProperty("ngrinder.etc.hosts", "");
	private final String consoleIP = System.getProperty("ngrinder.console.ip", "127.0.0.1");
	private final List<String> allowedHost = new ArrayList<String>();
	private final List<String> writeAllowedDirectory = new ArrayList<String>();
	private final List<String> deleteAllowedDirectory = new ArrayList<String>();

	{
		this.init();
	}

	void init() {
		ngrinderContext = System.getProperty("ngrinder.context", "agent");
		if (isControllerContext()) {
			controllerHomeDir = resolveControllerHomeDir();
			controllerHomeTmpDir = (controllerHomeDir + File.separator + NGRINDER_CONTROLLER_TEMP_FOLDER);
		}
		this.initAccessOfDirectories();
		this.initAccessOfHosts();
	}

	private String resolveControllerHomeDir() {
		String userHomeFromEnv = System.getenv("NGRINDER_HOME");
		String userHomeFromProperty = System.getProperty("ngrinder.home");
		String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		if (StringUtils.isEmpty(userHome)) {
			userHome = System.getProperty("user.home") + File.separator + NGRINDER_CONTROLLER_DEFAULT_FOLDER;
		} else if (StringUtils.startsWith(userHome, "~" + File.separator)) {
			userHome = System.getProperty("user.home") + File.separator + userHome.substring(2);
		} else if (StringUtils.startsWith(userHome, "." + File.separator)) {
			userHome = System.getProperty("user.dir") + File.separator + userHome.substring(2);
		}
		return FilenameUtils.normalize(userHome);
	}

	/**
	 * Set default accessed of directories. <br>
	 */
	private void initAccessOfDirectories() {
		workDirectory = normalize(new File(workDirectory).getAbsolutePath(), null);
		String logDirectory;
		if (workDirectory != null && !workDirectory.isEmpty()) {
			logDirectory = workDirectory.substring(0, workDirectory.lastIndexOf(File.separator));
			logDirectory = logDirectory.substring(0, workDirectory.lastIndexOf(File.separator)) + File.separator
					+ "log";
		} else {
			logDirectory = "log";
		}

		if (isNotEmpty(pythonCache)) {
			writeAllowedDirectory.add(pythonCache);
		}
		if (isNotEmpty(pythonHome)) {
			writeAllowedDirectory.add(pythonHome);
		}
		if (isNotEmpty(pythonPath)) {
			writeAllowedDirectory.add(pythonPath);
		}
		if (isNotEmpty(pythonCache)) {
			writeAllowedDirectory.add(pythonCache);
		}
		writeAllowedDirectory.add(workDirectory);
		writeAllowedDirectory.add(logDirectory);
		writeAllowedDirectory.add(getTempDirectoryPath());
		deleteAllowedDirectory.add(workDirectory);
	}

	private static boolean isNotEmpty(String str) {
		return str != null && str.length() != 0;
	}

	// -----------------------------------------------------------------------

	/**
	 * Returns the path to the system temporary directory.
	 *
	 * @return the path to the system temporary directory.
	 * @since Commons IO 2.0
	 */
	private static String getTempDirectoryPath() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * Get ip address of target hosts. <br>
	 * if target hosts 'a.com:1.1.1.1' add 'a.com' & '1.1.1.1' <br>
	 * if target hosts ':1.1.1.1' add : '1.1.1.1' <br>
	 * if target hosts '1.1.1.1' add : '1.1.1.1' <br>
	 * <br>
	 * Add controller host<br>
	 */
	private void initAccessOfHosts() {
		String[] hostsList = etcHosts.split(",");
		for (String hosts : hostsList) {
			String[] addresses = hosts.split(":");
			if (addresses.length > 1) {
				allowedHost.add(addresses[0]);
				allowedHost.add(addresses[addresses.length - 1]);
			} else {
				allowedHost.add(hosts);
			}
		}

		// add controller host
		allowedHost.add(consoleIP);
		try {
			java.security.Security.setProperty("networkaddress.cache.ttl", "0");
		} catch (Exception e) {
			// Fall through
		}
	}


	@Override
	public void checkPermission(Permission permission) {
		if (permission instanceof java.lang.RuntimePermission) {
			// except setSecurityManager
			String permissionName = permission.getName();
			if ("setSecurityManager".equals(permissionName)) {
				processSetSecurityManagerAction();
			}
		} else if (permission instanceof java.security.UnresolvedPermission) {
			throw new SecurityException("java.security.UnresolvedPermission is not allowed.");
		} else if (permission instanceof java.awt.AWTPermission) {
			throw new SecurityException("java.awt.AWTPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.AuthPermission) {
			throw new SecurityException("javax.security.auth.AuthPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.PrivateCredentialPermission) {
			throw new SecurityException("javax.security.auth.PrivateCredentialPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.kerberos.DelegationPermission) {
			throw new SecurityException("javax.security.auth.kerberos.DelegationPermission is not allowed.");
		} else if (permission instanceof javax.security.auth.kerberos.ServicePermission) {
			throw new SecurityException("javax.security.auth.kerberos.ServicePermission is not allowed.");
		} else if (permission instanceof javax.sound.sampled.AudioPermission) {
			throw new SecurityException("javax.sound.sampled.AudioPermission is not allowed.");
		}
	}

	protected void processSetSecurityManagerAction() throws SecurityException {
		throw new SecurityException("java.lang.RuntimePermission: setSecurityManager is not allowed.");
	}

	@Override
	public void checkPermission(Permission permission, Object context) {
		this.checkPermission(permission);
	}

	@Override
	public void checkRead(String file) {
		if (isControllerContext()) {
			if (file != null) {
				this.fileAccessReadAllowed(file);
			}
		}
	}

	@Override
	public void checkRead(String file, Object context) {
		if (isControllerContext()) {
			if (file != null) {
				this.fileAccessReadAllowed(file);
			}
		}
	}

	@Override
	public void checkRead(FileDescriptor fd) {
	}

	@Override
	public void checkWrite(String file) {
		this.fileAccessWriteAllowed(file);
	}

	@Override
	public void checkDelete(String file) {
		this.fileAccessDeleteAllowed(file);
	}

	@Override
	public void checkExec(String cmd) {
		throw new SecurityException("Cmd execution of " + cmd + " is not allowed.");
	}

	/**
	 * Check if the given file is safe to read.
	 *
	 * @param file file path
	 */
	private void fileAccessReadAllowed(String file) {
		String filePath = normalize(file, workDirectory);
		if (filePath != null && filePath.startsWith(controllerHomeDir)) {
			if (!filePath.startsWith(workDirectory) && !filePath.startsWith(controllerHomeTmpDir)) {
				throw new SecurityException("File Read access on " + file + "(" + filePath + ") is not allowed.");
			}
		}
	}

	/**
	 * File write access is allowed <br>
	 * on "agent.exec.folder".
	 *
	 * @param file file path
	 */
	private void fileAccessWriteAllowed(String file) {
		if (file != null && (file.contains("log/test_") || file.contains("log\\test_"))) {
			return;
		}

		String filePath = normalize(file, workDirectory);
		for (String dir : writeAllowedDirectory) {
			if (filePath != null && filePath.startsWith(dir)) {
				return;
			}
		}
		throw new SecurityException("File write access on " + file + "(" + filePath + ") is not allowed.");
	}

	/**
	 * File delete access is allowed <br>
	 * on "agent.exec.folder".
	 *
	 * @param file file path
	 */
	private void fileAccessDeleteAllowed(String file) {
		String filePath = normalize(file, workDirectory);
		for (String dir : deleteAllowedDirectory) {
			if (filePath != null && filePath.startsWith(dir)) {
				return;
			}
		}
		throw new SecurityException("File delete access on " + file + "(" + filePath + ") is not allowed.");
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		throw new SecurityException("Multicast on " + maddr.toString() + " is not always allowed.");
	}

	@Override
	public void checkConnect(String host, int port) {
		this.netWorkAccessAllowed(host);
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		this.netWorkAccessAllowed(host);
	}

	private String normalize(String filename, String workingDirectory) {
		if (getPrefixLength(filename) == 0 && workingDirectory != null) {
			filename = workingDirectory + File.separator + filename;
		}
		return doNormalize(filename);
	}

	/**
	 * NetWork access is allowed on "ngrinder.etc.hosts".
	 *
	 * @param host host name
	 */
	private void netWorkAccessAllowed(String host) {
		if (allowedHost.contains(host)) {
			return;
		}
		throw new SecurityException("NetWork access on " + host + " is not allowed. Please add " + host
				+ " on the target host setting.");
	}

	/**
	 * check current ngrinde context is controller.
	 */
	private boolean isControllerContext() {
		return ngrinderContext.equalsIgnoreCase(NGRINDER_CONTEXT_CONTROLLER);
	}

	/**
	 * The system separator character.
	 */
	private static final char SYSTEM_SEPARATOR = File.separatorChar;

	/**
	 * The Unix separator character.
	 */
	private static final char UNIX_SEPARATOR = '/';

	/**
	 * The Windows separator character.
	 */
	private static final char WINDOWS_SEPARATOR = '\\';

	/**
	 * The separator character that is the opposite of the system separator.
	 */
	private static final char OTHER_SEPARATOR;

	static {
		if (isSystemWindows()) {
			OTHER_SEPARATOR = UNIX_SEPARATOR;
		} else {
			OTHER_SEPARATOR = WINDOWS_SEPARATOR;
		}
	}

	// -----------------------------------------------------------------------

	/**
	 * Determines if Windows file system is in use.
	 *
	 * @return true if the system is Windows
	 */
	private static boolean isSystemWindows() {
		return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
	}

	/**
	 * Internal method to perform the normalization.
	 *
	 * @param filename the filename
	 * @return the normalized filename
	 */
	private static String doNormalize(String filename) {
		if (filename == null) {
			return null;
		}
		int size = filename.length();
		if (size == 0) {
			return filename;
		}
		int prefix = getPrefixLength(filename);
		if (prefix < 0) {
			return null;
		}

		char[] array = new char[size + 2]; // +1 for possible extra slash, +2 for arraycopy
		filename.getChars(0, filename.length(), array, 0);

		for (int i = 0; i < array.length; i++) {
			if (array[i] == OTHER_SEPARATOR) {
				array[i] = SYSTEM_SEPARATOR;
			}
		}

		// add extra separator on the end to simplify code below
		boolean lastIsDirectory = true;
		if (array[size - 1] != SYSTEM_SEPARATOR) {
			array[size++] = SYSTEM_SEPARATOR;
			lastIsDirectory = false;
		}

		// adjoining slashes
		for (int i = prefix + 1; i < size; i++) {
			if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == SYSTEM_SEPARATOR) {
				System.arraycopy(array, i, array, i - 1, size - i);
				size--;
				i--;
			}
		}

		// dot slash
		for (int i = prefix + 1; i < size; i++) {
			if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.' && (i == prefix + 1 || array[i - 2] == SYSTEM_SEPARATOR)) {
				if (i == size - 1) {
					lastIsDirectory = true;
				}
				System.arraycopy(array, i + 1, array, i - 1, size - i);
				size -= 2;
				i--;
			}
		}

		// double dot slash
		outer:
		for (int i = prefix + 2; i < size; i++) {
			if (array[i] == SYSTEM_SEPARATOR && array[i - 1] == '.' && array[i - 2] == '.'
					&& (i == prefix + 2 || array[i - 3] == SYSTEM_SEPARATOR)) {
				if (i == prefix + 2) {
					return null;
				}
				if (i == size - 1) {
					lastIsDirectory = true;
				}
				int j;
				for (j = i - 4; j >= prefix; j--) {
					if (array[j] == SYSTEM_SEPARATOR) {
						// remove b/../ from a/b/../c
						System.arraycopy(array, i + 1, array, j + 1, size - i);
						size -= (i - j);
						i = j + 1;
						continue outer;
					}
				}
				// remove a/../ from a/../c
				System.arraycopy(array, i + 1, array, prefix, size - i);
				size -= (i + 1 - prefix);
				i = prefix + 1;
			}
		}

		if (size <= 0) { // should never be less than 0
			return "";
		}
		if (size <= prefix) { // should never be less than prefix
			return new String(array, 0, size);
		}
		if (lastIsDirectory) {
			return new String(array, 0, size); // keep trailing separator
		}
		return new String(array, 0, size - 1); // lose trailing separator
	}

	// -----------------------------------------------------------------------

	/**
	 * Returns the length of the filename prefix, such as <code>C:/</code> or <code>~/</code>.
	 * <p/>
	 * This method will handle a file in either Unix or Windows format.
	 * <p/>
	 * The prefix length includes the first slash in the full filename if applicable. Thus, it is possible that the
	 * length returned is greater than the length of the input string.
	 * <p/>
	 * <pre>
	 * Windows:
	 * a\b\c.txt           --> ""          --> relative
	 * \a\b\c.txt          --> "\"         --> current drive absolute
	 * C:a\b\c.txt         --> "C:"        --> drive relative
	 * C:\a\b\c.txt        --> "C:\"       --> absolute
	 * \\server\a\b\c.txt  --> "\\server\" --> UNC
	 *
	 * Unix:
	 * a/b/c.txt           --> ""          --> relative
	 * /a/b/c.txt          --> "/"         --> absolute
	 * ~/a/b/c.txt         --> "~/"        --> current user
	 * ~                   --> "~/"        --> current user (slash added)
	 * ~user/a/b/c.txt     --> "~user/"    --> named user
	 * ~user               --> "~user/"    --> named user (slash added)
	 * </pre>
	 * <p/>
	 * The output will be the same irrespective of the machine that the code is running on. ie. both Unix and Windows
	 * prefixes are matched regardless.
	 *
	 * @param filename the filename to find the prefix in, null returns -1
	 * @return the length of the prefix, -1 if invalid or null
	 */
	private static int getPrefixLength(String filename) {
		if (filename == null) {
			return -1;
		}
		int len = filename.length();
		if (len == 0) {
			return 0;
		}
		char ch0 = filename.charAt(0);
		if (ch0 == ':') {
			return -1;
		}
		if (len == 1) {
			if (ch0 == '~') {
				return 2; // return a length greater than the input
			}
			return (isSeparator(ch0) ? 1 : 0);
		} else {
			if (ch0 == '~') {
				int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
				int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
				if (posUnix == -1 && posWin == -1) {
					return len + 1; // return a length greater than the input
				}
				posUnix = (posUnix == -1 ? posWin : posUnix);
				posWin = (posWin == -1 ? posUnix : posWin);
				return Math.min(posUnix, posWin) + 1;
			}
			char ch1 = filename.charAt(1);
			if (ch1 == ':') {
				ch0 = Character.toUpperCase(ch0);
				if (ch0 >= 'A' && ch0 <= 'Z') {
					if (len == 2 || !isSeparator(filename.charAt(2))) {
						return 2;
					}
					return 3;
				}
				return -1;

			} else if (isSeparator(ch0) && isSeparator(ch1)) {
				int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
				int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
				if ((posUnix == -1 && posWin == -1) || posUnix == 2 || posWin == 2) {
					return -1;
				}
				posUnix = (posUnix == -1 ? posWin : posUnix);
				posWin = (posWin == -1 ? posUnix : posWin);
				return Math.min(posUnix, posWin) + 1;
			} else {
				return (isSeparator(ch0) ? 1 : 0);
			}
		}
	}

	// -----------------------------------------------------------------------

	/**
	 * Checks if the character is a separator.
	 *
	 * @param ch the character to check
	 * @return true if it is a separator character
	 */
	private static boolean isSeparator(char ch) {
		return (ch == UNIX_SEPARATOR) || (ch == WINDOWS_SEPARATOR);
	}

}
