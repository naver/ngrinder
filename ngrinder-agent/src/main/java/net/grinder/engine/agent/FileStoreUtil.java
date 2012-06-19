package net.grinder.engine.agent;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.grinder.engine.agent.FileStore.FileStoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStoreUtil {
	private static final Logger LOG = LoggerFactory.getLogger(FileStoreUtil.class);
	
	public static File getFileStore () {
    	String localHostName;
		try {
			localHostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage());
			localHostName = "LOCALHOST";
		}
    	String fileStorePath = "./" + localHostName + "-file-store";
        //String currentPath = fileStorePath + File.separator + "current";
        //String propFilePath = currentPath + File.separator + "grinder.properties";
    	//File propertyFile = new File (propFilePath);    	

		try {
			FileStore fs = new FileStore(new File(fileStorePath), LOG);
			File currentDir = fs.getDirectory().getFile();
			File propertyFile = new File(currentDir, "grinder.properties");
	    	return propertyFile;
		} catch (FileStoreException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}
}
