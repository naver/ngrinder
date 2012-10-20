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

import org.apache.commons.lang.StringUtils;

import liquibase.database.Database;
import liquibase.database.structure.type.BigIntType;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.DoubleType;
import liquibase.database.structure.type.TinyIntType;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.typeconversion.core.AbstractTypeConverter;
import liquibase.exception.UnexpectedLiquibaseException;

/**
 * Liquibase Cubrid type converter.
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
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getDataType(java.lang.String,
	 * java.lang.Boolean)
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
	 * @see liquibase.database.typeconversion.TypeConverter#supports(liquibase.database.Database)
	 */
	@Override
	public boolean supports(Database database) {
		return "cubrid".equals(database.getTypeName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.typeconversion.core.AbstractTypeConverter#getBigIntType()
	 */
	@Override
	public BigIntType getBigIntType() {
		return new BigIntType("decimal");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.typeconversion.core.AbstractTypeConverter#getBooleanType()
	 */
	@Override
	public BooleanType getBooleanType() {
		return new CubridBooleanType("char(1)");
	}

	/**
	 * Custom boolean type converter for cubrid T or F representation for booolean type.
	 * 
	 * @author JunHo Yoon
	 * 
	 */
	public class CubridBooleanType extends BooleanType {
		/**
		 * Constructor.
		 * 
		 * @param type
		 *            native type for boolean
		 */
		public CubridBooleanType(String type) {
			super(type);
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
	            if ("T".equals(StringUtils.trim((String)value))) {
	            	return booleanType.getTrueBooleanValue();
	            } else if ("F".equals(StringUtils.trim((String)value)) || StringUtils.isEmpty((String)value)) {
	            	return booleanType.getFalseBooleanValue();
	            } else {
	                throw new UnexpectedLiquibaseException("Unknown boolean value: "+value);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.typeconversion.core.AbstractTypeConverter#getDateTimeType()
	 */
	@Override
	public DateTimeType getDateTimeType() {
		return new DateTimeType("TIMESTAMP");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.typeconversion.core.AbstractTypeConverter#getDoubleType()
	 */
	@Override
	public DoubleType getDoubleType() {
		return new DoubleType("DOUBLE PRECISION");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.database.typeconversion.core.AbstractTypeConverter#getTinyIntType()
	 */
	@Override
	public TinyIntType getTinyIntType() {
		return new TinyIntType("SMALLINT");
	}

}