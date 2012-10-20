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
	 * @param type
	 *            native type for boolean
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
