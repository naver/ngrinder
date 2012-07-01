package org.ngrinder;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.ngrinder.testutil.GrinderAgentStarter;


public class MultiAgentsTest {

    
    @Test
    public void startMultipleAgents () throws IOException {
        File propFile1 = new File("src/test/resources/grinder1.properties");
        GrinderAgentStarter.startLocalGrinderAgent(propFile1);
        
        File propFile2 = new File("src/test/resources/grinder2.properties");
        GrinderAgentStarter.startLocalGrinderAgent(propFile2);
        
        File propFile3 = new File("src/test/resources/grinder3.properties");
        GrinderAgentStarter.startLocalGrinderAgent(propFile3);

        System.in.read();
    }
}
