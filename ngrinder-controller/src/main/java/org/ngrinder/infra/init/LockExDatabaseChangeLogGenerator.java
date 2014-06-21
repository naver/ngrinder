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

import java.net.InetAddress;
import java.sql.Timestamp;

import liquibase.database.Database;
import liquibase.database.core.CUBRIDDatabase;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.LockExDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;

/**
 * Customized {@link liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator}. <br/>
 * previous {@link liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator} runs update sql 
 * which only checks if the lock status equals to BooleanType true value. 
 * It does not work if the prevous field type is changed due to
 * hibernate dialect changes.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class LockExDatabaseChangeLogGenerator extends 
				AbstractSqlGenerator<LockExDatabaseChangeLogStatement> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement,
	 * liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
	 */
	@Override
	public ValidationErrors validate(LockExDatabaseChangeLogStatement statement, Database database,
					SqlGeneratorChain sqlGeneratorChain) {
		return new ValidationErrors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.sqlgenerator.SqlGenerator#generateSql(liquibase.statement.SqlStatement,
	 * liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
	 */
	@Override
	public Sql[] generateSql(LockExDatabaseChangeLogStatement statement, Database database,
					SqlGeneratorChain sqlGeneratorChain) {
		InetAddress localHost;
		try {
			localHost = NetUtil.getLocalHost();
		} catch (Exception e) {
			throw new UnexpectedLiquibaseException(e);
		}

		String liquibaseSchema = database.getLiquibaseSchemaName();
		UpdateStatement updateStatement = new UpdateStatement(liquibaseSchema,
						database.getDatabaseChangeLogLockTableName());
		updateStatement.addNewColumnValue("LOCKED", true);
		updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
		updateStatement.addNewColumnValue("LOCKEDBY", localHost.getHostName()
						+ " (" + localHost.getHostAddress() + ")");
		String whereClause = database.escapeColumnName(liquibaseSchema, 
						database.getDatabaseChangeLogTableName(), "ID")
						+ " = 1 AND ";

		if (database instanceof CUBRIDDatabase) {
			whereClause = whereClause
							+ "( "
							+ database.escapeColumnName(liquibaseSchema, 
											database.getDatabaseChangeLogTableName(),
											"LOCKED")
							+ " = "
							+ TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType()
											.getFalseBooleanValue()
							+ " OR "
							+ database.escapeColumnName(liquibaseSchema, 
											database.getDatabaseChangeLogTableName(),
											"LOCKED") + " = '0')";
		} else {
			whereClause = whereClause
							+ database.escapeColumnName(liquibaseSchema, 
											database.getDatabaseChangeLogTableName(),
											"LOCKED")
							+ " = "
							+ TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType()
											.getFalseBooleanValue();
		}
		updateStatement.setWhereClause(whereClause);

		return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

	}
}
