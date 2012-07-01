package org.ngrinder;

import java.io.IOException;

import net.grinder.ConsoleExt;

import org.junit.Test;


public class MultiConsoleTest {

    ConsoleExt c1;
    ConsoleExt c2;
    ConsoleExt c3;
    
    @Test
    public void testConsoleProp () {
        
        try {
            System.setProperty("grinder.console.consolePort", "11123");
            new Thread(new Runnable() {                
                    @Override
                    public void run() {
                        c1 = new ConsoleExt();
                        c1.startConsole();
                    }
                }
            ).start();
            
            Thread.sleep(1000);

            System.setProperty("grinder.console.consolePort", "12123");
            new Thread(new Runnable() {                
                    @Override
                    public void run() {
                        c2 = new ConsoleExt();
                        c2.startConsole();
                    }
                }
            ).start();
            
            Thread.sleep(1000);
            
            System.setProperty("grinder.console.consolePort", "13123");
            new Thread(new Runnable() {                
                    @Override
                    public void run() {
                        c3 = new ConsoleExt();
                        c3.startConsole();
                    }
                }
            ).start();
            
            Thread.sleep(3000);
            
            
            System.in.read();
            
            c1.shutdownConsole();
            c2.shutdownConsole();
            c3.shutdownConsole();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
