package com.nhncorp.ngrinder.listener;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.grinder.common.GrinderProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhncorp.ngrinder.monitor.LogLoader;
import com.nhncorp.ngrinder.util.HttpUtils;

public class AgentInitDataListener implements ServletContextListener {
	private static final Logger LOG = LoggerFactory.getLogger(AgentInitDataListener.class);

    @Override    
    public void contextInitialized(ServletContextEvent evt) {
        String tomcatRoot = System.getProperty("catalina.base");
        LogLoader.setTomcatRoot(tomcatRoot);
   
		try {
			String grinderProperties = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	        //String serverPath = grinderProperties.substring(0, grinderProperties.indexOf("webapps"))+File.separator+"conf";
	        String webRootDir = Thread.currentThread().getContextClassLoader().getResource("").getPath().replaceAll("/WEB-INF/classes/$", "");
	        String[] dirs = webRootDir.split("/");

	    	GrinderProperties properties = null;
	    	
			Map<String, Object> params = new HashMap<String, Object>();
  
			File propertiesFile = new File(grinderProperties+File.separator+"grinder.properties");
			properties = new GrinderProperties(propertiesFile);
			String consoleIP = properties.getProperty("grinder.consoleHost");
			String consolePort = properties.getProperty("nGrinder.controllerPort");
			String agentPort = properties.getProperty("nGrinder.agentPort");

	        String agentAddress = InetAddress.getLocalHost().getHostAddress();
			params.put("ip", agentAddress);
			params.put("port", agentPort);
			params.put("appName", dirs[dirs.length-1]);


			LOG.debug("consoleInfo consoleAddress:{}:{}", consoleIP,consolePort);
			
			String res = HttpUtils.execute(consoleIP, new Integer(consolePort), "","autoRegister", "register", params);
			LOG.debug("consoleInfo HttpUtil.execute:{}", res);
			
		}catch(Exception e){
			LOG.error("AgentInitDataListener contextInitialized  getting Agent information  error !");
			e.printStackTrace();
		}
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        //nothing to do at this time
    }

}
