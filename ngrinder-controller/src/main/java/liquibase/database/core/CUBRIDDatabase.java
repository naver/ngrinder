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
package liquibase.database.core;

import java.math.BigInteger;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

/**
 * Liquibase will not be modified,this is just made its support CUBRID DB.
 * 
 * @author Matt
 * @author JunHo Yoon
 * @since 3.0
 */
public class CUBRIDDatabase extends AbstractDatabase {
	public static final String PRODUCT_NAME = "cubrid";

	public String getShortName() {
		return "cubrid";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.Database#getDefaultDriver(java.lang.String)
	 */
	@Override
	public String getDefaultDriver(String url) {
		if (url.startsWith("jdbc:CUBRID")) {
			return "cubrid.jdbc.driver.CUBRIDDriver";
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.servicelocator.PrioritizedService#getPriority()
	 */
	@Override
	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#isCorrectDatabaseImplementation(liquibase.database.DatabaseConnection)
	 */
	@Override
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#supportsSequences()
	 */
	@Override
	public boolean supportsSequences() {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#supportsInitiallyDeferrableColumns()
	 */
	@Override
	public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getCurrentDateTimeFunction()
	 */
	@Override
	public String getCurrentDateTimeFunction() {
		if (currentDateTimeFunction != null) {
			return currentDateTimeFunction;
		}

		return "NOW()";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getLineComment()
	 */
	@Override
	public String getLineComment() {
		return "-- ";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getAutoIncrementClause()
	 */
	@Override
	protected String getAutoIncrementClause() {
		return "AUTO_INCREMENT";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#generateAutoIncrementBy(java.math.BigInteger)
	 */
	@Override
	protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getAutoIncrementOpening()
	 */
	@Override
	protected String getAutoIncrementOpening() {
		return "";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getAutoIncrementClosing()
	 */
	@Override
	protected String getAutoIncrementClosing() {
		return "";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getAutoIncrementStartWithClause()
	 */
	@Override
	protected String getAutoIncrementStartWithClause() {
		return "=%d";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getConcatSql(java.lang.String[])
	 */
	@Override
	public String getConcatSql(String... values) {
		StringBuffer returnString = new StringBuffer();
		returnString.append("CONCAT_WS(");
		for (String value : values) {
			returnString.append(value).append(", ");
		}

		return returnString.toString().replaceFirst(", $", ")");
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#supportsTablespaces()
	 */
	@Override
	public boolean supportsTablespaces() {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getDefaultDatabaseSchemaName()
	 */
	@Override
	protected String getDefaultDatabaseSchemaName() throws DatabaseException {
		return getConnection().getCatalog();
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#convertRequestedSchemaToSchema(java.lang.String)
	 */
	@Override
	public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
		if (requestedSchema == null) {
			return getDefaultDatabaseSchemaName();
		}
		return requestedSchema;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#convertRequestedSchemaToCatalog(java.lang.String)
	 */
	@Override
	public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
		return requestedSchema;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#escapeDatabaseObject(java.lang.String)
	 */
	@Override
	public String escapeDatabaseObject(String objectName) {
		return "`" + objectName + "`";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#escapeIndexName(java.lang.String, java.lang.String)
	 */
	@Override
	public String escapeIndexName(String schemaName, String indexName) {
		return escapeDatabaseObject(indexName);
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#supportsForeignKeyDisable()
	 */
	@Override
	public boolean supportsForeignKeyDisable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#disableForeignKeyChecks()
	 */
	@Override
	public boolean disableForeignKeyChecks() throws DatabaseException {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#enableForeignKeyChecks()
	 */
	@Override
	public void enableForeignKeyChecks() {
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getTypeName()
	 */
	public String getTypeName() {
		return "cubrid";
	}
}