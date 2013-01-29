/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.infra.init;

import liquibase.database.Database;
import liquibase.database.core.CUBRIDDatabase;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.ModifyDataTypeStatement;

/**
 * Modify Column type sql generator. Modified to support Cubrid.
 * 
 * @since 3.1.1
 * @author JunHo Yoon
 */
public class ModifyDataTypeExGenerator extends AbstractSqlGenerator<ModifyDataTypeStatement> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#warn(liquibase.statement.SqlStatement,
	 * liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
	 */
	@Override
	public Warnings warn(ModifyDataTypeStatement modifyDataTypeStatement, Database database,
					SqlGeneratorChain sqlGeneratorChain) {
		Warnings warnings = super.warn(modifyDataTypeStatement, database, sqlGeneratorChain);

		if (database instanceof MySQLDatabase
						&& !modifyDataTypeStatement.getNewDataType().toLowerCase().contains("varchar")) {
			warnings.addWarning("modifyDataType will lose primary key/autoincrement/not null settings for mysql."
							+ "  Use <sql> and re-specify all configuration if this is the case");
		}

		return warnings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement,
	 * liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
	 */
	@Override
	public ValidationErrors validate(ModifyDataTypeStatement statement, Database database,
					SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", statement.getTableName());
		validationErrors.checkRequiredField("columnName", statement.getColumnName());
		validationErrors.checkRequiredField("newDataType", statement.getNewDataType());

		return validationErrors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.sqlgenerator.SqlGenerator#generateSql(liquibase.statement.SqlStatement,
	 * liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
	 */
	@Override
	public Sql[] generateSql(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain chain) {
		String alterTable = "ALTER TABLE "
						+ database.escapeTableName(statement.getSchemaName(), statement.getTableName());

		// add "MODIFY"
		alterTable += " " + getModifyString(database) + " ";

		// add column name
		alterTable += database.escapeColumnName(statement.getSchemaName(), statement.getTableName(),
						statement.getColumnName());

		alterTable += getPreDataTypeString(database); // adds a space if nothing else

		// add column type
		alterTable += TypeConverterFactory.getInstance().findTypeConverter(database)
						.getDataType(statement.getNewDataType(), false);

		return new Sql[] { new UnparsedSql(alterTable) };
	}

	private String getModifyString(Database database) {
		if (database instanceof CUBRIDDatabase || database instanceof SybaseASADatabase
						|| database instanceof SybaseDatabase || database instanceof MySQLDatabase
						|| database instanceof OracleDatabase || database instanceof MaxDBDatabase
						|| database instanceof InformixDatabase) {
			return "MODIFY";
		} else {
			return "ALTER COLUMN";
		}
	}

	private String getPreDataTypeString(Database database) {
		if (database instanceof DerbyDatabase || database instanceof DB2Database) {
			return " SET DATA TYPE ";
		} else if (database instanceof CUBRIDDatabase || database instanceof SybaseASADatabase
						|| database instanceof SybaseDatabase || database instanceof MSSQLDatabase
						|| database instanceof MySQLDatabase || database instanceof HsqlDatabase
						|| database instanceof H2Database || database instanceof CacheDatabase
						|| database instanceof OracleDatabase || database instanceof MaxDBDatabase
						|| database instanceof InformixDatabase) {
			return " ";
		} else {
			return " TYPE ";
		}
	}
}
