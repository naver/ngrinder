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
package org.hibernate.dialect;

import java.sql.Types;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * Hibernate CUBRID Dialect.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class CUBRIDExDialect extends Dialect {
	@Override
	protected String getIdentityColumnString() {
		return "auto_increment"; // starts with 1, implicitly
	}

	@Override
	public String getIdentitySelectString(final String table, final String column, final int type) {
		return "select last_insert_id()";
	}

	private static final int VAR_CHAR_SIZE = 4000;
	private static final int VARING_BIT = 2000;

	/**
	 * Constructor.
	 */
	public CUBRIDExDialect() {
		super();
		registerColumnType(Types.BIT, "bit(8)");
		registerColumnType(Types.BIGINT, "decimal");
		registerColumnType(Types.BOOLEAN, "char(2)");
		registerColumnType(Types.SMALLINT, "smallint");
		registerColumnType(Types.TINYINT, "smallint");
		registerColumnType(Types.INTEGER, "integer");
		registerColumnType(Types.CHAR, "char(1)");
		registerColumnType(Types.VARCHAR, VAR_CHAR_SIZE, "varchar($l)");
		registerColumnType(Types.FLOAT, "float");
		registerColumnType(Types.DOUBLE, "double");
		registerColumnType(Types.DATE, "date");
		registerColumnType(Types.TIME, "time");
		registerColumnType(Types.TIMESTAMP, "timestamp");
		registerColumnType(Types.VARBINARY, VARING_BIT, "bit varying($l)");
		registerColumnType(Types.VARBINARY, "bit varying(2000)");
		registerColumnType(Types.NUMERIC, "numeric($p,$s)");
		registerColumnType(Types.BLOB, "blob");
		registerColumnType(Types.CLOB, "string");

		getDefaultProperties().setProperty(Environment.USE_STREAMS_FOR_BINARY, "true");
		getDefaultProperties().setProperty(Environment.STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE);

		registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
		registerFunction("trim", new StandardSQLFunction("trim"));
		registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.INTEGER));
		registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.INTEGER));
		registerFunction("coalesce", new StandardSQLFunction("coalesce"));
		registerFunction("nullif", new StandardSQLFunction("nullif"));
		registerFunction("abs", new StandardSQLFunction("abs"));
		registerFunction("mod", new StandardSQLFunction("mod"));
		registerFunction("upper", new StandardSQLFunction("upper"));
		registerFunction("lower", new StandardSQLFunction("lower"));

		registerFunction("power", new StandardSQLFunction("power"));
		registerFunction("stddev", new StandardSQLFunction("stddev"));
		registerFunction("variance", new StandardSQLFunction("variance"));
		registerFunction("round", new StandardSQLFunction("round"));
		registerFunction("trunc", new StandardSQLFunction("trunc"));
		registerFunction("ceil", new StandardSQLFunction("ceil"));
		registerFunction("floor", new StandardSQLFunction("floor"));
		registerFunction("ltrim", new StandardSQLFunction("ltrim"));
		registerFunction("rtrim", new StandardSQLFunction("rtrim"));
		registerFunction("nvl", new StandardSQLFunction("nvl"));
		registerFunction("nvl2", new StandardSQLFunction("nvl2"));
		registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
		registerFunction("chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER));
		registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
		registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.TIMESTAMP));
		registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE));
		registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER));
		registerFunction("instrb", new StandardSQLFunction("instrb", StandardBasicTypes.INTEGER));
		registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
		registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
		registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING));
		registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
		registerFunction("substrb", new StandardSQLFunction("substrb", StandardBasicTypes.STRING));
		registerFunction("translate", new StandardSQLFunction("translate", StandardBasicTypes.STRING));
		registerFunction("add_months", new StandardSQLFunction("add_months", StandardBasicTypes.DATE));
		registerFunction("months_between", new StandardSQLFunction("months_between", StandardBasicTypes.FLOAT));

		registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
		registerFunction("current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME, false));
		registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP,
				false));
		registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.DATE, false));
		registerFunction("systime", new NoArgSQLFunction("systime", StandardBasicTypes.TIME, false));
		registerFunction("systimestamp", new NoArgSQLFunction("systimestamp", StandardBasicTypes.TIMESTAMP, false));
		registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false));
		registerFunction("rownum", new NoArgSQLFunction("rownum", StandardBasicTypes.LONG, false));
		registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
	}

	@Override
	public String getAddColumnString() {
		return "add";
	}

	@Override
	public String getSequenceNextValString(final String sequenceName) {
		return "select " + sequenceName + ".next_value from table({1}) as T(X)";
	}

	@Override
	public String getCreateSequenceString(final String sequenceName) {
		return "create serial " + sequenceName;
	}

	@Override
	public String getDropSequenceString(final String sequenceName) {
		return "drop serial " + sequenceName;
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public String getQuerySequencesString() {
		return "select name from db_serial";
	}

	@Override
	public boolean dropConstraints() {
		return false;
	}

	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public String getLimitString(final String sql, final boolean hasOffset) {
		// CUBRID 8.3.0 support limit
		return sql + (hasOffset ? " limit ?, ?" : " limit ?");
	}

	@Override
	public boolean useMaxForLimit() {
		return true;
	}

	@Override
	public boolean forUpdateOfColumns() {
		return true;
	}

	@Override
	public char closeQuote() {
		return ']';
	}

	@Override
	public char openQuote() {
		return '[';
	}

	@Override
	public boolean hasAlterTable() {
		return false;
	}

	@Override
	public String getForUpdateString() {
		return " ";
	}

	@Override
	public boolean supportsUnionAll() {
		return true;
	}

	@Override
	public boolean supportsCommentOn() {
		return false;
	}

	@Override
	public boolean supportsTemporaryTables() {
		return false;
	}

	@Override
	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return "select systimestamp from table({1}) as T(X)";
	}

	@Override
	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	@Override
	public String toBooleanValueString(final boolean bool) {
		return bool ? "1" : "0";
	}

	@Override
	public boolean supportsIfExistsBeforeTableName() {
		return true;
	}
}