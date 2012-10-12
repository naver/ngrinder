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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.Database;
import liquibase.database.structure.ForeignKeyInfo;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.jvm.JdbcDatabaseSnapshotGenerator;

/**
 * Liquibase will not be modified,this is just made its support CUBRID DB.
 * 
 * @author Matt
 * @author JunHo Yoon
 * @since 3.0
 */
public class CUBRIDDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

	protected ForeignKeyInfo fillForeignKeyInfo(ResultSet rs) throws DatabaseException, SQLException {
		ForeignKeyInfo fkInfo = super.fillForeignKeyInfo(rs);
		if (fkInfo.getKeySeq() == 0)
			fkInfo.setReferencesUniqueColumn(true);
		return fkInfo;
	}

	public boolean supports(Database database) {
		return database instanceof CUBRIDDatabase;
	}

	public int getPriority(Database database) {
		return PRIORITY_DATABASE;
	}

	@Override
	protected void readSequences(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData)
			throws DatabaseException {
	}

}