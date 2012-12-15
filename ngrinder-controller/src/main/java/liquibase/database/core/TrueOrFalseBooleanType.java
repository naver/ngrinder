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
import liquibase.database.structure.type.BooleanType;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.UnexpectedLiquibaseException;

import org.apache.commons.lang.StringUtils;

/**
 * BooleanType which is represented as "T" and "F" in char(1) field.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class TrueOrFalseBooleanType extends BooleanType {
	/**
	 * Constructor.
	 * 
	 */
	public TrueOrFalseBooleanType() {
		super("char(1)");
	}

	@Override
	public String getTrueBooleanValue() {
		return "'T'";
	}

	@Override
	public String getFalseBooleanValue() {
		return "'F'";
	};

	@Override
	public String convertObjectToString(Object value, Database database) {
		if (value == null) {
			return null;
		} else if (value.toString().equalsIgnoreCase("null")) {
			return "null";
		}

		String returnValue;
		TypeConverter converter = TypeConverterFactory.getInstance().findTypeConverter(database);
		BooleanType booleanType = converter.getBooleanType();
		if (value instanceof String) {
			String trim = StringUtils.trim((String) value);
			if ("T".equals(trim)) {
				return booleanType.getTrueBooleanValue();
			} else if ("F".equals(trim) || StringUtils.isEmpty((String) value) || "0".equals(trim)) {
				return booleanType.getFalseBooleanValue();
			} else {
				throw new UnexpectedLiquibaseException("Unknown boolean value: " + value);
			}
		} else if (value instanceof Integer) {
			if (Integer.valueOf(1).equals(value)) {
				returnValue = booleanType.getTrueBooleanValue();
			} else {
				returnValue = booleanType.getFalseBooleanValue();
			}
		} else if (value instanceof Long) {
			if (Long.valueOf(1).equals(value)) {
				returnValue = booleanType.getTrueBooleanValue();
			} else {
				returnValue = booleanType.getFalseBooleanValue();
			}
		} else if (((Boolean) value)) {
			returnValue = booleanType.getTrueBooleanValue();
		} else {
			returnValue = booleanType.getFalseBooleanValue();
		}

		return returnValue;
	}
}
