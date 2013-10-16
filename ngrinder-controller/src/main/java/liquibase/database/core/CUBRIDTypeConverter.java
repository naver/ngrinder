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

import liquibase.database.Database;
import liquibase.database.structure.type.BigIntType;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.DoubleType;
import liquibase.database.structure.type.TinyIntType;
import liquibase.database.typeconversion.core.AbstractTypeConverter;

/**
 * {@link liquibase.database.typeconversion.TypeConverter} for CUBRID.
 * 
 * @author Matt
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public class CUBRIDTypeConverter extends AbstractTypeConverter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getDataType
	 * (java.lang.String, java.lang.Boolean)
	 */
	@Override
	public DataType getDataType(String columnTypeString, Boolean autoIncrement) {
		if (columnTypeString != null) {
			return super.getDataType(columnTypeString, autoIncrement);
		} else {
			return super.getDataType("VARCHAR", autoIncrement);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.typeconversion.TypeConverter#getPriority()
	 */
	@Override
	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.TypeConverter#supports(liquibase.database
	 * .Database)
	 */
	@Override
	public boolean supports(Database database) {
		return "cubrid".equals(database.getTypeName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getBigIntType
	 * ()
	 */
	@Override
	public BigIntType getBigIntType() {
		return new BigIntType("decimal");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getBooleanType
	 * ()
	 */
	@Override
	public BooleanType getBooleanType() {
		return new TrueOrFalseBooleanType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getDateTimeType
	 * ()
	 */
	@Override
	public DateTimeType getDateTimeType() {
		return new DateTimeType("TIMESTAMP");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getDoubleType
	 * ()
	 */
	@Override
	public DoubleType getDoubleType() {
		return new DoubleType("DOUBLE PRECISION");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getTinyIntType
	 * ()
	 */
	@Override
	public TinyIntType getTinyIntType() {
		return new TinyIntType("SMALLINT");
	}

}