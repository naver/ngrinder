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
package liquibase.database.core;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.Database;
import liquibase.database.structure.ForeignKeyInfo;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.jvm.JdbcDatabaseSnapshotGenerator;

/**
 * Snapshot generator for CUBRID.
 * 
 * @author Matt
 * @author JunHo Yoon
 * @since 3.0
 */
public class CUBRIDDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.snapshot.jvm.JdbcDatabaseSnapshotGenerator#fillForeignKeyInfo
	 * (java.sql.ResultSet)
	 */
	@Override
	protected ForeignKeyInfo fillForeignKeyInfo(ResultSet rs) throws DatabaseException, SQLException {
		ForeignKeyInfo fkInfo = super.fillForeignKeyInfo(rs);
		if (fkInfo.getKeySeq() == 0) {
			fkInfo.setReferencesUniqueColumn(true);
		}
		return fkInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.snapshot.DatabaseSnapshotGenerator#supports(liquibase.database
	 * .Database)
	 */
	@Override
	public boolean supports(Database database) {
		return database instanceof CUBRIDDatabase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.snapshot.DatabaseSnapshotGenerator#getPriority(liquibase.database
	 * .Database)
	 */
	@Override
	public int getPriority(Database database) {
		return PRIORITY_DATABASE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.snapshot.jvm.JdbcDatabaseSnapshotGenerator#readSequences(liquibase
	 * .snapshot. DatabaseSnapshot, java.lang.String, java.sql.DatabaseMetaData)
	 */
	@Override
	protected void readSequences(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData)
			throws DatabaseException {
	}
}