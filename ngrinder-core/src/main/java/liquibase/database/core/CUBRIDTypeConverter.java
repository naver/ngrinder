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
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.DoubleType;
import liquibase.database.structure.type.TinyIntType;
import liquibase.database.typeconversion.core.AbstractTypeConverter;

/**
 * Liquibase will not be modified,this is just made its support CUBRID DB
 */
public class CUBRIDTypeConverter extends AbstractTypeConverter {

	public DataType getDataType(String columnTypeString, Boolean autoIncrement) {
		if (columnTypeString != null)
			return super.getDataType(columnTypeString, autoIncrement);
		else
			return super.getDataType("VARCHAR", autoIncrement);
	}

	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	public boolean supports(Database database) {
		return "cubrid".equals(database.getTypeName());
	}

	public BooleanType getBooleanType() {
		return new BooleanType.NumericBooleanType("char(1)");
	}

	public DateTimeType getDateTimeType() {
		return new DateTimeType("TIMESTAMP");
	}

	public DoubleType getDoubleType() {
		return new DoubleType("DOUBLE PRECISION");
	}

	public TinyIntType getTinyIntType() {
		return new TinyIntType("SMALLINT");
	}

}