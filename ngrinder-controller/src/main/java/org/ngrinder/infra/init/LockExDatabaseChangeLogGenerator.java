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

public class LockExDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockExDatabaseChangeLogStatement> {

	public ValidationErrors validate(LockExDatabaseChangeLogStatement statement, Database database,
					SqlGeneratorChain sqlGeneratorChain) {
		return new ValidationErrors();
	}

	public Sql[] generateSql(LockExDatabaseChangeLogStatement statement, Database database,
					SqlGeneratorChain sqlGeneratorChain) {
		String liquibaseSchema = null;
		liquibaseSchema = database.getLiquibaseSchemaName();

		InetAddress localHost;
		try {
			localHost = NetUtil.getLocalHost();
		} catch (Exception e) {
			throw new UnexpectedLiquibaseException(e);
		}

		UpdateStatement updateStatement = new UpdateStatement(liquibaseSchema,
						database.getDatabaseChangeLogLockTableName());
		updateStatement.addNewColumnValue("LOCKED", true);
		updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
		updateStatement.addNewColumnValue("LOCKEDBY", localHost.getHostName() + " (" + localHost.getHostAddress() + ")");
		String whereClause = database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID")
						+ " = 1 AND ";

		if (database instanceof CUBRIDDatabase) {
			whereClause = whereClause
							+ "( "
							+ database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(),
											"LOCKED")
							+ " = "
							+ TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType()
											.getFalseBooleanValue()
							+ " OR "
							+ database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(),
											"LOCKED") + " = '0')";
		} else {
			whereClause = whereClause
							+ database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(),
											"LOCKED")
							+ " = "
							+ TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType()
											.getFalseBooleanValue();
		}
		updateStatement.setWhereClause(whereClause);

		return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

	}
}
