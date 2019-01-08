package org.ngrinder.infra.hazelcast;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fest.assertions.Assertions.assertThat;

public class HazelcastServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private HazelcastService hazelcastService;

	@Autowired
	private Config config;

	@Test(expected = IllegalArgumentException.class)
	public void findClusterMemberTest() {
		String currentRegion = config.getRegion();
		assertThat(hazelcastService.findClusterMember(currentRegion)).isNotNull();
		assertThat(hazelcastService.findClusterMember("NotRegisteredRegion"));
	}
}
