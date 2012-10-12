package org.hibernate.dialect;

import java.sql.Types;

public class H2ExDialect extends H2Dialect {
	public H2ExDialect() {
		super();
		registerColumnType(Types.FLOAT, "double");
	}
}
