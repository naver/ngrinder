package liquibase.database.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;

@DataTypeInfo(name = "boolean", aliases = {"java.sql.Types.BOOLEAN", "java.lang.Boolean", "bit", "bool"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DATABASE)
public class BooleanTypeEx extends BooleanType {

	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		if (database instanceof H2Database) {
			return new DatabaseDataType("CHAR(5)");
		}
		return super.toDatabaseDataType(database);
	}
}
