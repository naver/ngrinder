package org.ngrinder.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * the BO to operate hosts file.
 *
 * @author dani
 */
@Service
public class HostsService {
	private static final Logger LOG = LoggerFactory.getLogger(HostsService.class);
	
	private static final String CHARSET = "UTF-8";
	
	private static final SimpleDateFormat dateFormat = 
			new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault());
	
	private static boolean firstTime = true; 
	
	private static final File HOSTS_FILE = 
			new File(FileUtil.getHostsFileName(System.getProperty("os.name")));
	
	private static final String BACKUP_HOSTS_FILE_PREFIX = 
			HostsService.class.getClassLoader().getResource(".").getPath() + "." + HOSTS_FILE.getName();
	
	/** The original backup , just in case */
	private static final File ORIGIN_HOSTS_FILE = 
			new File(BACKUP_HOSTS_FILE_PREFIX + ".ngrinder.origin_" + dateFormat.format(new Date()));
		
		
	private static final File BACKUP_HOSTS_FILE = 
			new File(BACKUP_HOSTS_FILE_PREFIX +".ngrinder.bak");
	
	

	/**
	 * update hosts file content, first back up
	 * @param hostsContent
	 * @throws IOException
	 */
	public void updateHostsFile(String hostsContent) throws Exception {
		if (firstTime) {
			FileUtils.copyFile(HOSTS_FILE, ORIGIN_HOSTS_FILE);
			firstTime = false;
		}
		FileUtils.copyFile(HOSTS_FILE, BACKUP_HOSTS_FILE);
		
		//not to replace the hosts file content
		//FileUtils.writeStringToFile(HOSTS_FILE, hostsContent, CHARSET);
		
		ArrayList<String> appendContent = new ArrayList<String>(2);
		appendContent.add(IOUtils.LINE_SEPARATOR);
		appendContent.add(hostsContent);
		FileUtils.writeLines(HOSTS_FILE, appendContent);
		LOG.info("Success in updating hosts file");
	}
	
	/**
	 * recover the hosts file from backup
	 * @throws IOException
	 */
	public void recoverHostsFile() throws Exception{
		String content = FileUtils.readFileToString(BACKUP_HOSTS_FILE, CHARSET);
		FileUtils.writeStringToFile(HOSTS_FILE, content, CHARSET);
		LOG.info("Success in recovering hosts file");
	}
	
    /**
     * get Agent Server time
     * 
     * @param null
     */
    public String getAgentDate() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(date);
        return time ;
    }
	
}
