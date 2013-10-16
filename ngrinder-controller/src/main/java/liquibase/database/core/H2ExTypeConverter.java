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

import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.FloatType;
import liquibase.database.typeconversion.core.H2TypeConverter;

/**
 * {@link liquibase.database.typeconversion.TypeConverter} for H2.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public class H2ExTypeConverter extends H2TypeConverter {
	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getFloatType
	 * ()
	 */
	@Override
	public FloatType getFloatType() {
		return new FloatType("DOUBLE");
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
}