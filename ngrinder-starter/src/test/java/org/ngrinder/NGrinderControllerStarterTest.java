package org.ngrinder;


import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class NGrinderControllerStarterTest {
	@Test
	public void test1() throws Exception {
		System.setProperty("unit-test", "true");
		NGrinderControllerStarter.main(new String[]
				{

						"-p=9090"
				}
		);
	}

	@Test
	public void testEasyClusterConfiguration() {
		NGrinderControllerStarter.ClusterMode.easy.parseArgs(new String[]{
				"-cluster-port=2022"
		});
	}
}
