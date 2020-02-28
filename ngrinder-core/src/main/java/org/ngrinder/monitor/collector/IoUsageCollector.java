package org.ngrinder.monitor.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 采集磁盘IO使用率
 */
public class IoUsageCollector {

	private static final Logger LOGGER = LoggerFactory.getLogger(IoUsageCollector.class);
	private static IoUsageCollector INSTANCE = new IoUsageCollector();

	public IoUsageCollector(){

	}

	public static IoUsageCollector getInstance(){
		return INSTANCE;
	}

	/**
	 * @Purpose:采集磁盘IO使用率
	 * @return float,磁盘IO使用率,小于1
	 */

	public float getIoUsage() {
		//LOGGER.info("开始收集磁盘IO使用率");
		float ioUsage = 0.0f;
		Process pro;
		Runtime r = Runtime.getRuntime();
		try {
            String cmd = "iostat -d -x 1 2" + "|tail -2f";
			String[] command = { "/bin/sh", "-c", cmd};

			pro = r.exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			int count =  0;
			while((line=in.readLine()) != null){
				if(++count >= 1){
					//LOGGER.info("数据"+line);
					String[] temp = line.split("\\s+");
					if(temp.length > 1){
						float util =  Float.parseFloat(temp[temp.length-1]);
						ioUsage = (ioUsage>util)?ioUsage:util;
					}
				}
			}
			if(ioUsage >= 0){
				//LOGGER.info("本节点磁盘IO使用率为: " + ioUsage);
				ioUsage /= 100;
			}
			in.close();
			pro.destroy();
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			LOGGER.error("IoUsage发生InstantiationException. " + e.getMessage());
			LOGGER.error(sw.toString());
		}
		return ioUsage;
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		while(true){
			System.out.println(IoUsageCollector.getInstance().getIoUsage());
			Thread.sleep(2000);
		}
	}

}
