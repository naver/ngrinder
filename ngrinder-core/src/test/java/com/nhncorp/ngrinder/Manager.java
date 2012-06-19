package com.nhncorp.ngrinder;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.grinder.common.GrinderProperties;
import net.grinder.console.model.SampleModel;
import net.grinder.util.Directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhncorp.ngrinder.grinder.ConsoleExt;
import com.nhncorp.ngrinder.testutil.GrinderAgentStarter;
import com.nhncorp.ngrinder.util.ThreadUtil;

public class Manager implements ManagerMBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(Manager.class);
	
	int port1 = 11123;
	int port2 = 12123;
	int port3 = 13123;
	
	ConsoleExt console1;
	ConsoleExt console2;
	ConsoleExt console3;
	
	File propFile1;
	File propFile2;
	File propFile3;
	
	boolean running1 = false;
	boolean running2 = false;
	boolean running3 = false;

	private void startConsole(final ConsoleExt console) {
        new Thread(new Runnable() {
                @Override
                public void run() {                	
                	console.startConsole();
                }
            }
        ).start();
	}
	
	@Override
	public void startConsole1() {
		console1 = new ConsoleExt(port1, "console1");
		startConsole (console1);
	}
	
	@Override
	public void startAgent1() {
        propFile1 = new File("src/test/resources/grinder1.properties");
        GrinderAgentStarter.startLocalGrinderAgent(propFile1);
        ThreadUtil.sleep(2000);
        int agtCount = console1.getProcessControl().getNumberOfLiveAgents();
        LOG.debug("Agent count of console1:{}", agtCount);
	}

	public void startTest(ConsoleExt console, File propertiesFile) {
		try {
			GrinderProperties properties = new GrinderProperties(propertiesFile);
			final Directory directory = new Directory(propertiesFile.getParentFile());
			// Ensure the properties passed to the agent has a relative
			// associated path.
			properties.setAssociatedFile(directory.rebaseFile(propertiesFile));

			SampleModel model = (SampleModel) console.getConsoleContainer()
					.getComponent(SampleModel.class);
			model.stop();
			model.start();
			console.getProcessControl().startWorkerProcesses(properties);
			
			while (true) {
				//get test status
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void startTest1() {
		startTest(console1, propFile1);
	}

	@Override
	public void stopConsole1() {
		console1.shutdownConsole();
	}

	@Override
	public void stpoTest1() {
		console1.getProcessControl().resetWorkerProcesses();
		SampleModel model = (SampleModel) console1.getConsoleContainer()
				.getComponent(SampleModel.class);
		model.reset();
	}

	@Override
	public void startConsole2() {
		console2 = new ConsoleExt(port2, "console2");
		startConsole (console2);
	}

	@Override
	public void startAgent2() {
        File propFile = new File("src/test/resources/grinder2.properties");
        GrinderAgentStarter.startLocalGrinderAgent(propFile);
        ThreadUtil.sleep(2000);
        int agtCount = console2.getProcessControl().getNumberOfLiveAgents();
        LOG.debug("Agent count of console2:{}", agtCount);
	}
	
	@Override
	public void startTest2() {
		startTest(console2, propFile2);
	}

	@Override
	public void stopConsole2() {
		console2.shutdownConsole();
	}

	@Override
	public void stpoTest2() {
		console2.getProcessControl().resetWorkerProcesses();
		SampleModel model = (SampleModel) console2.getConsoleContainer()
				.getComponent(SampleModel.class);
		model.reset();
	}

	@Override
	public void startConsole3() {
		console3 = new ConsoleExt(port3, "console3");
		startConsole (console3);
	}

	@Override
	public void startAgent3() {
        File propFile = new File("src/test/resources/grinder3.properties");
        GrinderAgentStarter.startLocalGrinderAgent(propFile);
        ThreadUtil.sleep(2000);
        int agtCount = console3.getProcessControl().getNumberOfLiveAgents();
        LOG.debug("Agent count of console3:{}", agtCount);
	}
	
	@Override
	public void startTest3() {
		startTest(console3, propFile3);
	}

	@Override
	public void stopConsole3() {
		console3.shutdownConsole();
	}

	@Override
	public void stpoTest3() {
		console3.getProcessControl().resetWorkerProcesses();
		SampleModel model = (SampleModel) console3.getConsoleContainer()
				.getComponent(SampleModel.class);
		model.reset();
	}
	
	public static void main(String[] args) {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			ObjectName name = new ObjectName("com.nhncorp.mbeans:type=ConsoleManager");

			Manager mbean = new Manager();

			mbs.registerMBean(mbean, name);

			System.out.println("Waiting forever...");
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
