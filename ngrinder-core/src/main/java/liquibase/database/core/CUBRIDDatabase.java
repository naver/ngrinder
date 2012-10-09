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
 * Liquibase will not be modified,this is just made its support CUBRID DB
 */
public class CUBRIDDatabase extends AbstractDatabase  {
	public static final String PRODUCT_NAME = "cubrid";
	 public String getShortName() {
	        return "cubrid";
	 }
	 public String getDefaultDriver(String url) {
	        if (url.startsWith("jdbc:CUBRID")) {
	            return "cubrid.jdbc.driver.CUBRIDDriver";
	        }
	        return null;
	    }
	    public int getPriority() {
	        return PRIORITY_DEFAULT;
	    }

	    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
	        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
	    }
	
	    
	    public boolean supportsSequences() {
	        return false;
	    }

	    public boolean supportsInitiallyDeferrableColumns() {
	        return false;
	    }

	    public String getCurrentDateTimeFunction() {
	        if (currentDateTimeFunction != null) {
	            return currentDateTimeFunction;
	        }
	        
	        return "NOW()";
	    }

	    
	    public String getLineComment() {
	        return "-- ";
	    }

	    
	    protected String getAutoIncrementClause() {
	    	return "AUTO_INCREMENT";
	    }    

	    
	    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
	    	return false;
	    }
	   
	    
	    protected String getAutoIncrementOpening() {
	    	return "";
	    }
	    
	    
	    protected String getAutoIncrementClosing() {
	    	return "";
	    }
	    
	    
	    protected String getAutoIncrementStartWithClause() {
	    	return "=%d";
	    }
	    
	    
	    public String getConcatSql(String ... values) {
	        StringBuffer returnString = new StringBuffer();
	        returnString.append("CONCAT_WS(");
	        for (String value : values) {
	            returnString.append(value).append(", ");
	        }

	        return returnString.toString().replaceFirst(", $", ")");
	    }

	    public boolean supportsTablespaces() {
	        return false;
	    }


	    
	    protected String getDefaultDatabaseSchemaName() throws DatabaseException {
	            return getConnection().getCatalog();
	    }

	    
	    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
	        if (requestedSchema == null) {
	            return getDefaultDatabaseSchemaName();
	        }
	        return requestedSchema;
	    }

	    
	    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
	        return requestedSchema;
	    }

	    
	    public String escapeDatabaseObject(String objectName) {
	        return "`"+objectName+"`";
	    }

	    
	    public String escapeIndexName(String schemaName, String indexName) {
	        return escapeDatabaseObject(indexName);
	    }

	    
	    
	    public boolean supportsForeignKeyDisable() {
	        return false;
	    }

	    
	    public boolean disableForeignKeyChecks() throws DatabaseException {
	        return false;
	    }

	    
	    public void enableForeignKeyChecks() {
	    }
		public String getTypeName() {
			return "cubrid";
		}
}