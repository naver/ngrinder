package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConsoleEntryTest {

	@Test
	public void testEqualsObject() {
		ConsoleEntry console1 = new ConsoleEntry(111);
		ConsoleEntry console2 = new ConsoleEntry(111);
		assertThat(console1, is(console2));
		assertThat(console1.hashCode(), is(console2.hashCode()));
		
		console2.setPort(222);
		assertThat(console1, not(console2));
		assertThat(console1.hashCode(), not(console2.hashCode()));

		console2.setPort(null);
		assertThat(console1, not(console2));
		assertThat(console1.hashCode(), not(console2.hashCode()));
	}

}
