/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
import ;
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
