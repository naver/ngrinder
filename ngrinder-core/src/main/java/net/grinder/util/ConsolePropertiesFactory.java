package net.grinder.util;

import java.io.File;

import net.grinder.SingleConsole;
import net.grinder.console.model.ConsoleProperties;

public class ConsolePropertiesFactory {
	public static ConsoleProperties createEmptyConsoleProperties() {
		try {
			return new ConsoleProperties(SingleConsole.resources, new File(
					"tmp"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
