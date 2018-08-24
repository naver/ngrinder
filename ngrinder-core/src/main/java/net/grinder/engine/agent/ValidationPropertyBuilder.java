package net.grinder.engine.agent;

import net.grinder.common.GrinderProperties;
import net.grinder.util.Directory;

public class ValidationPropertyBuilder extends PropertyBuilder {
	public ValidationPropertyBuilder(GrinderProperties properties, Directory baseDirectory, boolean securityEnabled, String securityLevel, String hostString, String hostName) {
		super(properties, baseDirectory, securityEnabled, securityLevel, hostString, hostName);
	}

	@Override
	protected StringBuilder addContext(StringBuilder jvmArguments) {
		return jvmArguments.append( " -Dngrinder.context=controller ");
	}
}
