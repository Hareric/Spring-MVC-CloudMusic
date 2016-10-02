/*
   Copyright (C) 2005 MySQL AB

   This program is free software; you can redistribute it and/or modify
   it under the terms of version 2 of the GNU General Public License as 
   published by the Free Software Foundation.

   There are special exceptions to the terms and conditions of the GPL 
   as it is applied to this software. View the full text of the 
   exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
   software distribution.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package com.mysql.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class MysqlParameterMetadata implements ParameterMetaData {
	boolean returnSimpleMetadata = false;
	ResultSetMetaData metadata = null;
	int parameterCount = 0;
	
	
	MysqlParameterMetadata(Field[] fieldInfo, int parameterCount) {
		this.metadata = new ResultSetMetaData(fieldInfo, false);
		
		this.parameterCount = parameterCount;
	}
	
	/**
	 * Used for "fake" basic metadata for client-side prepared statements
	 * when we don't know the parameter types.
	 * 
	 * @param parameterCount
	 */
	MysqlParameterMetadata(int count) {
		this.parameterCount = count;
		this.returnSimpleMetadata = true;
	}
	
	public int getParameterCount() throws SQLException {
		return this.parameterCount;
	}

	public int isNullable(int arg0) throws SQLException {
		checkAvailable();
		
		return this.metadata.isNullable(arg0);
	}

	private void checkAvailable() throws SQLException {
		if (this.metadata == null || this.metadata.fields == null) {
			throw SQLError.createSQLException(
				"Parameter metadata not available for the given statement",
				SQLError.SQL_STATE_DRIVER_NOT_CAPABLE);
		}
	}

	public boolean isSigned(int arg0) throws SQLException {
		if (this.returnSimpleMetadata) {
			checkBounds(arg0);
			
			return false;
		}
		
		checkAvailable();
		
		return (this.metadata.isSigned(arg0));
	}

	public int getPrecision(int arg0) throws SQLException {
		if (this.returnSimpleMetadata) {
			checkBounds(arg0);
			
			return 0;
		}
		
		checkAvailable();
		
		return (this.metadata.getPrecision(arg0));
	}

	public int getScale(int arg0) throws SQLException {
		if (this.returnSimpleMetadata) {
			checkBounds(arg0);
			
			return 0;
		}
		
		checkAvailable();
		
		return (this.metadata.getScale(arg0));
	}

	public int getParameterType(int arg0) throws SQLException {
		if (this.returnSimpleMetadata) {
			checkBounds(arg0);
			
			return Types.VARCHAR;
		}
		
		checkAvailable();
		
		return (this.metadata.getColumnType(arg0));
	}

	public String getParameterTypeName(int arg0) throws SQLException {
		if (this.returnSimpleMetadata) {
			checkBounds(arg0);
			
			return "VARCHAR";
		}
		
		checkAvailable();
		
		return (this.metadata.getColumnTypeName(arg0));
	}

	public String getParameterClassName(int arg0) throws SQLException {
		if (this.returnSimpleMetadata) {
			checkBounds(arg0);
			
			return "java.lang.String";
		}
		
		checkAvailable();
		
		return (this.metadata.getColumnClassName(arg0));
	}

	public int getParameterMode(int arg0) throws SQLException {
		return parameterModeIn;
	}
	
	private void checkBounds(int paramNumber) throws SQLException {
		if (paramNumber < 1) {
			throw SQLError.createSQLException("Parameter index of '" + paramNumber + 
					"' is invalid.",
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
		}
		
		if (paramNumber > this.parameterCount) {
			throw SQLError.createSQLException("Parameter index of '" + paramNumber + 
					"' is greater than number of parameters, which is '" + 
					this.parameterCount + "'.",
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
					
		}
	}
}
