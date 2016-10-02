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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DatabaseMetaData implementation that uses INFORMATION_SCHEMA available in
 * MySQL-5.0 and newer.
 * 
 * The majority of the queries in this code were built for Connector/OO.org by
 * Georg Richter (georg_at_mysql.com).
 */
public class DatabaseMetaDataUsingInfoSchema extends DatabaseMetaData {

	public DatabaseMetaDataUsingInfoSchema(Connection connToSet,
			String databaseToSet) {
		super(connToSet, databaseToSet);
	}

	private ResultSet executeMetadataQuery(PreparedStatement pStmt)
			throws SQLException {
		ResultSet rs = pStmt.executeQuery();
		((com.mysql.jdbc.ResultSet) rs).setOwningStatement(null);

		return rs;
	}

	/**
	 * Get a description of the access rights for a table's columns.
	 * <P>
	 * Only privileges matching the column name criteria are returned. They are
	 * ordered by COLUMN_NAME and PRIVILEGE.
	 * </p>
	 * <P>
	 * Each privilige description has the following columns:
	 * <OL>
	 * <li> <B>TABLE_CAT</B> String => table catalog (may be null) </li>
	 * <li> <B>TABLE_SCHEM</B> String => table schema (may be null) </li>
	 * <li> <B>TABLE_NAME</B> String => table name </li>
	 * <li> <B>COLUMN_NAME</B> String => column name </li>
	 * <li> <B>GRANTOR</B> => grantor of access (may be null) </li>
	 * <li> <B>GRANTEE</B> String => grantee of access </li>
	 * <li> <B>PRIVILEGE</B> String => name of access (SELECT, INSERT, UPDATE,
	 * REFRENCES, ...) </li>
	 * <li> <B>IS_GRANTABLE</B> String => "YES" if grantee is permitted to
	 * grant to others; "NO" if not; null if unknown </li>
	 * </ol>
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schema
	 *            a schema name; "" retrieves those without a schema
	 * @param table
	 *            a table name
	 * @param columnNamePattern
	 *            a column name pattern
	 * @return ResultSet each row is a column privilege description
	 * @throws SQLException
	 *             if a database access error occurs
	 * @see #getSearchStringEscape
	 */
	public java.sql.ResultSet getColumnPrivileges(String catalog,
			String schema, String table, String columnNamePattern)
			throws SQLException {
		if (columnNamePattern == null) {
			if (this.conn.getNullNamePatternMatchesAll()) {
				columnNamePattern = "%";
			} else {
				throw SQLError.createSQLException(
						"Column name pattern can not be NULL or empty.",
						SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
			}
		}

		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				catalog = this.database;
			}	
		}
		
		String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME,"
			 +"COLUMN_NAME, NULL AS GRANTOR, GRANTEE, PRIVILEGE_TYPE AS PRIVILEGE, IS_GRANTABLE FROM "
			 + "INFORMATION_SCHEMA.COLUMN_PRIVILEGES WHERE "
			 + "TABLE_SCHEMA LIKE ? AND "
			 + "TABLE_NAME =? AND COLUMN_NAME LIKE ? ORDER BY " 
			 + "COLUMN_NAME, PRIVILEGE_TYPE";
		
		PreparedStatement pStmt = null;
		
		try {
			pStmt = prepareMetaDataSafeStatement(sql);
			
			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, table);
			pStmt.setString(3, columnNamePattern);
			
			ResultSet rs = executeMetadataQuery(pStmt);
			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "TABLE_CAT", Types.CHAR, 64),
					new Field("", "TABLE_SCHEM", Types.CHAR, 1),
					new Field("", "TABLE_NAME", Types.CHAR, 64),
					new Field("", "COLUMN_NAME", Types.CHAR, 64),
					new Field("", "GRANTOR", Types.CHAR, 77),
					new Field("", "GRANTEE", Types.CHAR, 77),
					new Field("", "PRIVILEGE", Types.CHAR, 64),
					new Field("", "IS_GRANTABLE", Types.CHAR, 3)});
			
			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of table columns available in a catalog.
	 * <P>
	 * Only column descriptions matching the catalog, schema, table and column
	 * name criteria are returned. They are ordered by TABLE_SCHEM, TABLE_NAME
	 * and ORDINAL_POSITION.
	 * </p>
	 * <P>
	 * Each column description has the following columns:
	 * <OL>
	 * <li> <B>TABLE_CAT</B> String => table catalog (may be null) </li>
	 * <li> <B>TABLE_SCHEM</B> String => table schema (may be null) </li>
	 * <li> <B>TABLE_NAME</B> String => table name </li>
	 * <li> <B>COLUMN_NAME</B> String => column name </li>
	 * <li> <B>DATA_TYPE</B> short => SQL type from java.sql.Types </li>
	 * <li> <B>TYPE_NAME</B> String => Data source dependent type name </li>
	 * <li> <B>COLUMN_SIZE</B> int => column size. For char or date types this
	 * is the maximum number of characters, for numeric or decimal types this is
	 * precision. </li>
	 * <li> <B>BUFFER_LENGTH</B> is not used. </li>
	 * <li> <B>DECIMAL_DIGITS</B> int => the number of fractional digits </li>
	 * <li> <B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2) </li>
	 * <li> <B>NULLABLE</B> int => is NULL allowed?
	 * <UL>
	 * <li> columnNoNulls - might not allow NULL values </li>
	 * <li> columnNullable - definitely allows NULL values </li>
	 * <li> columnNullableUnknown - nullability unknown </li>
	 * </ul>
	 * </li>
	 * <li> <B>REMARKS</B> String => comment describing column (may be null)
	 * </li>
	 * <li> <B>COLUMN_DEF</B> String => default value (may be null) </li>
	 * <li> <B>SQL_DATA_TYPE</B> int => unused </li>
	 * <li> <B>SQL_DATETIME_SUB</B> int => unused </li>
	 * <li> <B>CHAR_OCTET_LENGTH</B> int => for char types the maximum number
	 * of bytes in the column </li>
	 * <li> <B>ORDINAL_POSITION</B> int => index of column in table (starting
	 * at 1) </li>
	 * <li> <B>IS_NULLABLE</B> String => "NO" means column definitely does not
	 * allow NULL values; "YES" means the column might allow NULL values. An
	 * empty string means nobody knows. </li>
	 * </ol>
	 * </p>
	 */
	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableName, String columnNamePattern) throws SQLException {
		if (columnNamePattern == null) {
			if (this.conn.getNullNamePatternMatchesAll()) {
				columnNamePattern = "%";
			} else {
				throw SQLError.createSQLException(
						"Column name pattern can not be NULL or empty.",
						SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
			}
		}

		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				catalog = this.database;
			}
		}

		StringBuffer sqlBuf = new StringBuffer("SELECT "
				+ "TABLE_SCHEMA AS TABLE_CAT, " + "NULL AS TABLE_SCHEM,"
				+ "TABLE_NAME," + "COLUMN_NAME,");
		MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");

		sqlBuf.append(" AS DATA_TYPE, ");

		if (conn.getCapitalizeTypeNames()) {
			sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS TYPE_NAME,");
		} else {
			sqlBuf.append("CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS TYPE_NAME,");
		}

		sqlBuf
				.append("CASE WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION ELSE CASE WHEN CHARACTER_MAXIMUM_LENGTH > " 
						+ Integer.MAX_VALUE + " THEN " + Integer.MAX_VALUE + 
						" ELSE CHARACTER_MAXIMUM_LENGTH END END AS COLUMN_SIZE, "
						+ MysqlIO.getMaxBuf() + " AS BUFFER_LENGTH,"
						+ "NUMERIC_SCALE AS DECIMAL_DIGITS,"
						+ "10 AS NUM_PREC_RADIX,"
						+ "CASE WHEN IS_NULLABLE='NO' THEN " + columnNoNulls + " ELSE CASE WHEN IS_NULLABLE='YES' THEN " + columnNullable + " ELSE " + columnNullableUnknown + " END END AS NULLABLE,"
						+ "COLUMN_COMMENT AS REMARKS,"
						+ "COLUMN_DEFAULT AS COLUMN_DEF,"
						+ "0 AS SQL_DATA_TYPE,"
						+ "0 AS SQL_DATETIME_SUB,"
						+ "CASE WHEN CHARACTER_OCTET_LENGTH > " + Integer.MAX_VALUE + " THEN " + Integer.MAX_VALUE + " ELSE CHARACTER_OCTET_LENGTH END AS CHAR_OCTET_LENGTH,"
						+ "ORDINAL_POSITION,"
						+ "IS_NULLABLE,"
						+ "NULL AS SCOPE_CATALOG,"
						+ "NULL AS SCOPE_SCHEMA,"
						+ "NULL AS SCOPE_TABLE,"
						+ "NULL AS SOURCE_DATA_TYPE,"
						+ "IF (EXTRA LIKE '%auto_increment%','YES','NO') AS IS_AUTOINCREMENT "
						+ "FROM INFORMATION_SCHEMA.COLUMNS WHERE "
						+ "TABLE_SCHEMA LIKE ? AND "
						+ "TABLE_NAME LIKE ? AND COLUMN_NAME LIKE ? "
						+ "ORDER BY TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION");

		PreparedStatement pStmt = null;

		try {
			pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());
			
			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, tableName);
			pStmt.setString(3, columnNamePattern);

			ResultSet rs = executeMetadataQuery(pStmt);

			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "TABLE_CAT", Types.CHAR, 255),
					new Field("", "TABLE_SCHEM", Types.CHAR, 0),
					new Field("", "TABLE_NAME", Types.CHAR, 255),
					new Field("", "COLUMN_NAME", Types.CHAR, 32),
					new Field("", "DATA_TYPE", Types.SMALLINT, 5),
					new Field("", "TYPE_NAME", Types.CHAR, 16),
					new Field("", "COLUMN_SIZE", Types.INTEGER, Integer
							.toString(Integer.MAX_VALUE).length()),
					new Field("", "BUFFER_LENGTH", Types.INTEGER, 10),
					new Field("", "DECIMAL_DIGITS", Types.INTEGER, 10),
					new Field("", "NUM_PREC_RADIX", Types.INTEGER, 10),
					new Field("", "NULLABLE", Types.INTEGER, 10),
					new Field("", "REMARKS", Types.CHAR, 0),
					new Field("", "COLUMN_DEF", Types.CHAR, 0),
					new Field("", "SQL_DATA_TYPE", Types.INTEGER, 10),
					new Field("", "SQL_DATETIME_SUB", Types.INTEGER, 10),
					new Field("", "CHAR_OCTET_LENGTH", Types.INTEGER, Integer
							.toString(Integer.MAX_VALUE).length()),
					new Field("", "ORDINAL_POSITION", Types.INTEGER, 10),
					new Field("", "IS_NULLABLE", Types.CHAR, 3),
					new Field("", "SCOPE_CATALOG", Types.CHAR, 255),
					new Field("", "SCOPE_SCHEMA", Types.CHAR, 255),
					new Field("", "SCOPE_TABLE", Types.CHAR, 255),
					new Field("", "SOURCE_DATA_TYPE", Types.SMALLINT, 10),
					new Field("", "IS_AUTOINCREMENT", Types.CHAR, 3) });
			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of the foreign key columns in the foreign key table
	 * that reference the primary key columns of the primary key table (describe
	 * how one table imports another's key.) This should normally return a
	 * single foreign key/primary key pair (most tables only import a foreign
	 * key from a table once.) They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
	 * FKTABLE_NAME, and KEY_SEQ.
	 * <P>
	 * Each foreign key column description has the following columns:
	 * <OL>
	 * <li> <B>PKTABLE_CAT</B> String => primary key table catalog (may be
	 * null) </li>
	 * <li> <B>PKTABLE_SCHEM</B> String => primary key table schema (may be
	 * null) </li>
	 * <li> <B>PKTABLE_NAME</B> String => primary key table name </li>
	 * <li> <B>PKCOLUMN_NAME</B> String => primary key column name </li>
	 * <li> <B>FKTABLE_CAT</B> String => foreign key table catalog (may be
	 * null) being exported (may be null) </li>
	 * <li> <B>FKTABLE_SCHEM</B> String => foreign key table schema (may be
	 * null) being exported (may be null) </li>
	 * <li> <B>FKTABLE_NAME</B> String => foreign key table name being exported
	 * </li>
	 * <li> <B>FKCOLUMN_NAME</B> String => foreign key column name being
	 * exported </li>
	 * <li> <B>KEY_SEQ</B> short => sequence number within foreign key </li>
	 * <li> <B>UPDATE_RULE</B> short => What happens to foreign key when
	 * primary is updated:
	 * <UL>
	 * <li> importedKeyCascade - change imported key to agree with primary key
	 * update </li>
	 * <li> importedKeyRestrict - do not allow update of primary key if it has
	 * been imported </li>
	 * <li> importedKeySetNull - change imported key to NULL if its primary key
	 * has been updated </li>
	 * </ul>
	 * </li>
	 * <li> <B>DELETE_RULE</B> short => What happens to the foreign key when
	 * primary is deleted.
	 * <UL>
	 * <li> importedKeyCascade - delete rows that import a deleted key </li>
	 * <li> importedKeyRestrict - do not allow delete of primary key if it has
	 * been imported </li>
	 * <li> importedKeySetNull - change imported key to NULL if its primary key
	 * has been deleted </li>
	 * </ul>
	 * </li>
	 * <li> <B>FK_NAME</B> String => foreign key identifier (may be null) </li>
	 * <li> <B>PK_NAME</B> String => primary key identifier (may be null) </li>
	 * </ol>
	 * </p>
	 * 
	 * @param primaryCatalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param primarySchema
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param primaryTable
	 *            a table name
	 * @param foreignCatalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param foreignSchema
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param foreignTable
	 *            a table name
	 * @return ResultSet each row is a foreign key column description
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public java.sql.ResultSet getCrossReference(String primaryCatalog,
			String primarySchema, String primaryTable, String foreignCatalog,
			String foreignSchema, String foreignTable) throws SQLException {
		if (primaryTable == null) {
			throw SQLError.createSQLException("Table not specified.",
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
		}

		if (primaryCatalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				primaryCatalog = this.database;	
			}
		}

		if (foreignCatalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				foreignCatalog = this.database;
			}
		}

		Field[] fields = new Field[14];
		fields[0] = new Field("", "PKTABLE_CAT", Types.CHAR, 255);
		fields[1] = new Field("", "PKTABLE_SCHEM", Types.CHAR, 0);
		fields[2] = new Field("", "PKTABLE_NAME", Types.CHAR, 255);
		fields[3] = new Field("", "PKCOLUMN_NAME", Types.CHAR, 32);
		fields[4] = new Field("", "FKTABLE_CAT", Types.CHAR, 255);
		fields[5] = new Field("", "FKTABLE_SCHEM", Types.CHAR, 0);
		fields[6] = new Field("", "FKTABLE_NAME", Types.CHAR, 255);
		fields[7] = new Field("", "FKCOLUMN_NAME", Types.CHAR, 32);
		fields[8] = new Field("", "KEY_SEQ", Types.SMALLINT, 2);
		fields[9] = new Field("", "UPDATE_RULE", Types.SMALLINT, 2);
		fields[10] = new Field("", "DELETE_RULE", Types.SMALLINT, 2);
		fields[11] = new Field("", "FK_NAME", Types.CHAR, 0);
		fields[12] = new Field("", "PK_NAME", Types.CHAR, 0);
		fields[13] = new Field("", "DEFERRABILITY", Types.INTEGER, 2);

		String sql = "SELECT "
				+ "A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,"
				+ "NULL AS PKTABLE_SCHEM,"
				+ "A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,"
				+ "A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME,"
				+ "A.TABLE_SCHEMA AS FKTABLE_CAT,"
				+ "NULL AS FKTABLE_SCHEM,"
				+ "A.TABLE_NAME AS FKTABLE_NAME, "
				+ "A.COLUMN_NAME AS FKCOLUMN_NAME, "
				+ "A.ORDINAL_POSITION AS KEY_SEQ,"
				+ importedKeyRestrict
				+ " AS UPDATE_RULE,"
				+ importedKeyRestrict
				+ " AS DELETE_RULE,"
				+ "A.CONSTRAINT_NAME AS FK_NAME,"
				+ "NULL AS PK_NAME,"
				+ importedKeyNotDeferrable
				+ " AS DEFERRABILITY "
				+ "FROM "
				+ "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A,"
				+ "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B "
				+ "WHERE "
				+ "A.TABLE_SCHEMA=B.TABLE_SCHEMA AND A.TABLE_NAME=B.TABLE_NAME "
				+ "AND "
				+ "A.CONSTRAINT_NAME=B.CONSTRAINT_NAME AND B.CONSTRAINT_TYPE IS NOT NULL "
				+ "AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? "
				+ "AND A.TABLE_SCHEMA LIKE ? AND A.TABLE_NAME=? " + "ORDER BY "
				+ "A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION";

		PreparedStatement pStmt = null;

		try {
			pStmt = prepareMetaDataSafeStatement(sql);
			if (primaryCatalog != null) {
				pStmt.setString(1, primaryCatalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, primaryTable);
			
			if (foreignCatalog != null) {
				pStmt.setString(3, foreignCatalog);
			} else {
				pStmt.setString(3, "%");
			}
			
			pStmt.setString(4, foreignTable);

			ResultSet rs = executeMetadataQuery(pStmt);
			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "PKTABLE_CAT", Types.CHAR, 255),
					new Field("", "PKTABLE_SCHEM", Types.CHAR, 0),
					new Field("", "PKTABLE_NAME", Types.CHAR, 255),
					new Field("", "PKCOLUMN_NAME", Types.CHAR, 32),
					new Field("", "FKTABLE_CAT", Types.CHAR, 255),
					new Field("", "FKTABLE_SCHEM", Types.CHAR, 0),
					new Field("", "FKTABLE_NAME", Types.CHAR, 255),
					new Field("", "FKCOLUMN_NAME", Types.CHAR, 32),
					new Field("", "KEY_SEQ", Types.SMALLINT, 2),
					new Field("", "UPDATE_RULE", Types.SMALLINT, 2),
					new Field("", "DELETE_RULE", Types.SMALLINT, 2),
					new Field("", "FK_NAME", Types.CHAR, 0),
					new Field("", "PK_NAME", Types.CHAR, 0),
					new Field("", "DEFERRABILITY", Types.INTEGER, 2) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of a foreign key columns that reference a table's
	 * primary key columns (the foreign keys exported by a table). They are
	 * ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and KEY_SEQ.
	 * <P>
	 * Each foreign key column description has the following columns:
	 * <OL>
	 * <li> <B>PKTABLE_CAT</B> String => primary key table catalog (may be
	 * null) </li>
	 * <li> <B>PKTABLE_SCHEM</B> String => primary key table schema (may be
	 * null) </li>
	 * <li> <B>PKTABLE_NAME</B> String => primary key table name </li>
	 * <li> <B>PKCOLUMN_NAME</B> String => primary key column name </li>
	 * <li> <B>FKTABLE_CAT</B> String => foreign key table catalog (may be
	 * null) being exported (may be null) </li>
	 * <li> <B>FKTABLE_SCHEM</B> String => foreign key table schema (may be
	 * null) being exported (may be null) </li>
	 * <li> <B>FKTABLE_NAME</B> String => foreign key table name being exported
	 * </li>
	 * <li> <B>FKCOLUMN_NAME</B> String => foreign key column name being
	 * exported </li>
	 * <li> <B>KEY_SEQ</B> short => sequence number within foreign key </li>
	 * <li> <B>UPDATE_RULE</B> short => What happens to foreign key when
	 * primary is updated:
	 * <UL>
	 * <li> importedKeyCascade - change imported key to agree with primary key
	 * update </li>
	 * <li> importedKeyRestrict - do not allow update of primary key if it has
	 * been imported </li>
	 * <li> importedKeySetNull - change imported key to NULL if its primary key
	 * has been updated </li>
	 * </ul>
	 * </li>
	 * <li> <B>DELETE_RULE</B> short => What happens to the foreign key when
	 * primary is deleted.
	 * <UL>
	 * <li> importedKeyCascade - delete rows that import a deleted key </li>
	 * <li> importedKeyRestrict - do not allow delete of primary key if it has
	 * been imported </li>
	 * <li> importedKeySetNull - change imported key to NULL if its primary key
	 * has been deleted </li>
	 * </ul>
	 * </li>
	 * <li> <B>FK_NAME</B> String => foreign key identifier (may be null) </li>
	 * <li> <B>PK_NAME</B> String => primary key identifier (may be null) </li>
	 * </ol>
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schema
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param table
	 *            a table name
	 * @return ResultSet each row is a foreign key column description
	 * @throws SQLException
	 *             if a database access error occurs
	 * @see #getImportedKeys
	 */
	public java.sql.ResultSet getExportedKeys(String catalog, String schema,
			String table) throws SQLException {
		// TODO: Can't determine actions using INFORMATION_SCHEMA yet...

		if (table == null) {
			throw SQLError.createSQLException("Table not specified.",
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
		}

		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				catalog = this.database;
			}	
		}

		String sql = "SELECT "
				+ "A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,"
				+ "NULL AS PKTABLE_SCHEM,"
				+ "A.REFERENCED_TABLE_NAME AS PKTABLE_NAME, "
				+ "A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, "
				+ "A.TABLE_SCHEMA AS FKTABLE_CAT,"
				+ "NULL AS FKTABLE_SCHEM,"
				+ "A.TABLE_NAME AS FKTABLE_NAME,"
				+ "A.COLUMN_NAME AS FKCOLUMN_NAME, "
				+ "A.ORDINAL_POSITION AS KEY_SEQ,"
				+ importedKeyRestrict
				+ " AS UPDATE_RULE,"
				+ importedKeyRestrict
				+ " AS DELETE_RULE,"
				+ "A.CONSTRAINT_NAME AS FK_NAME,"
				+ "NULL AS PK_NAME,"
				+ importedKeyNotDeferrable
				+ " AS DEFERRABILITY "
				+ "FROM "
				+ "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A,"
				+ "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B "
				+ "WHERE "
				+ "A.TABLE_SCHEMA=B.TABLE_SCHEMA AND A.TABLE_NAME=B.TABLE_NAME "
				+ "AND "
				+ "A.CONSTRAINT_NAME=B.CONSTRAINT_NAME AND B.CONSTRAINT_TYPE IS NOT NULL "
				+ "AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? "
				+ "ORDER BY A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION";

		PreparedStatement pStmt = null;

		try {
			pStmt = prepareMetaDataSafeStatement(sql);
			
			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, table);

			ResultSet rs = executeMetadataQuery(pStmt);

			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "PKTABLE_CAT", Types.CHAR, 255),
					new Field("", "PKTABLE_SCHEM", Types.CHAR, 0),
					new Field("", "PKTABLE_NAME", Types.CHAR, 255),
					new Field("", "PKCOLUMN_NAME", Types.CHAR, 32),
					new Field("", "FKTABLE_CAT", Types.CHAR, 255),
					new Field("", "FKTABLE_SCHEM", Types.CHAR, 0),
					new Field("", "FKTABLE_NAME", Types.CHAR, 255),
					new Field("", "FKCOLUMN_NAME", Types.CHAR, 32),
					new Field("", "KEY_SEQ", Types.SMALLINT, 2),
					new Field("", "UPDATE_RULE", Types.SMALLINT, 2),
					new Field("", "DELETE_RULE", Types.SMALLINT, 2),
					new Field("", "FK_NAME", Types.CHAR, 255),
					new Field("", "PK_NAME", Types.CHAR, 0),
					new Field("", "DEFERRABILITY", Types.INTEGER, 2) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}

	}

	/*
	 * 
	 * getTablePrivileges
	 * 
	 * if (getMysqlVersion() > 49999) { if (!strcasecmp("localhost",
	 * m_pSettings->pConnection->host)) { sprintf(user, "A.GRANTEE =
	 * \"'%s'@'localhost'\" OR A.GRANTEE LIKE \"'%'@'localhost'\"",
	 * m_pSettings->pConnection->user, m_pSettings->pConnection->user); } else {
	 * sprintf(user, "\"'%s'@'%s'\" LIKE A.GRANTEE",
	 * m_pSettings->pConnection->user, m_pSettings->pConnection->host); }
	 * 
	 * sprintf(query, "SELECT DISTINCT A.TABLE_CATALOG, B.TABLE_SCHEMA,
	 * B.TABLE_NAME, CURRENT_USER(), " \ "A.PRIVILEGE_TYPE FROM
	 * INFORMATION_SCHEMA.USER_PRIVILEGES A, INFORMATION_SCHEMA.TABLES B " \
	 * "WHERE B.TABLE_SCHEMA LIKE '%s' AND B.TABLE_NAME LIKE '%s' AND (%s) " \
	 * "UNION " \ "SELECT DISTINCT A.TABLE_CATALOG, B.TABLE_SCHEMA,
	 * B.TABLE_NAME, CURRENT_USER(), A.PRIVILEGE_TYPE " \ "FROM
	 * INFORMATION_SCHEMA.SCHEMA_PRIVILEGES A, INFORMATION_SCHEMA.TABLES B WHERE " \
	 * "B.TABLE_SCHEMA LIKE '%s' AND B.TABLE_NAME LIKE '%s' AND (%s) " \ "UNION "\
	 * "SELECT DISTINCT A.TABLE_CATALOG, A.TABLE_SCHEMA, A.TABLE_NAME,
	 * CURRENT_USER, A.PRIVILEGE_TYPE FROM " \
	 * "INFORMATION_SCHEMA.TABLE_PRIVILEGES A WHERE A.TABLE_SCHEMA LIKE '%s' AND
	 * A.TABLE_NAME LIKE '%s' " \ "AND (%s)", schemaName, tableName, user,
	 * schemaName, tableName, user, schemaName, tableName, user );
	 */

	/**
	 * Get a description of the primary key columns that are referenced by a
	 * table's foreign key columns (the primary keys imported by a table). They
	 * are ordered by PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
	 * <P>
	 * Each primary key column description has the following columns:
	 * <OL>
	 * <li> <B>PKTABLE_CAT</B> String => primary key table catalog being
	 * imported (may be null) </li>
	 * <li> <B>PKTABLE_SCHEM</B> String => primary key table schema being
	 * imported (may be null) </li>
	 * <li> <B>PKTABLE_NAME</B> String => primary key table name being imported
	 * </li>
	 * <li> <B>PKCOLUMN_NAME</B> String => primary key column name being
	 * imported </li>
	 * <li> <B>FKTABLE_CAT</B> String => foreign key table catalog (may be
	 * null) </li>
	 * <li> <B>FKTABLE_SCHEM</B> String => foreign key table schema (may be
	 * null) </li>
	 * <li> <B>FKTABLE_NAME</B> String => foreign key table name </li>
	 * <li> <B>FKCOLUMN_NAME</B> String => foreign key column name </li>
	 * <li> <B>KEY_SEQ</B> short => sequence number within foreign key </li>
	 * <li> <B>UPDATE_RULE</B> short => What happens to foreign key when
	 * primary is updated:
	 * <UL>
	 * <li> importedKeyCascade - change imported key to agree with primary key
	 * update </li>
	 * <li> importedKeyRestrict - do not allow update of primary key if it has
	 * been imported </li>
	 * <li> importedKeySetNull - change imported key to NULL if its primary key
	 * has been updated </li>
	 * </ul>
	 * </li>
	 * <li> <B>DELETE_RULE</B> short => What happens to the foreign key when
	 * primary is deleted.
	 * <UL>
	 * <li> importedKeyCascade - delete rows that import a deleted key </li>
	 * <li> importedKeyRestrict - do not allow delete of primary key if it has
	 * been imported </li>
	 * <li> importedKeySetNull - change imported key to NULL if its primary key
	 * has been deleted </li>
	 * </ul>
	 * </li>
	 * <li> <B>FK_NAME</B> String => foreign key name (may be null) </li>
	 * <li> <B>PK_NAME</B> String => primary key name (may be null) </li>
	 * </ol>
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schema
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param table
	 *            a table name
	 * @return ResultSet each row is a primary key column description
	 * @throws SQLException
	 *             if a database access error occurs
	 * @see #getExportedKeys
	 */
	public java.sql.ResultSet getImportedKeys(String catalog, String schema,
			String table) throws SQLException {
		if (table == null) {
			throw SQLError.createSQLException("Table not specified.",
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
		}

		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				catalog = this.database;
			}
		}

		String sql = "SELECT "
				+ "A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,"
				+ "NULL AS PKTABLE_SCHEM,"
				+ "A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,"
				+ "A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME,"
				+ "A.TABLE_SCHEMA AS FKTABLE_CAT,"
				+ "NULL AS FKTABLE_SCHEM,"
				+ "A.TABLE_NAME AS FKTABLE_NAME, "
				+ "A.COLUMN_NAME AS FKCOLUMN_NAME, "
				+ "A.ORDINAL_POSITION AS KEY_SEQ,"
				+ importedKeyRestrict
				+ " AS UPDATE_RULE,"
				+ importedKeyRestrict
				+ " AS DELETE_RULE,"
				+ "A.CONSTRAINT_NAME AS FK_NAME,"
				+ "NULL AS PK_NAME, "
				+ importedKeyNotDeferrable
				+ " AS DEFERRABILITY "
				+ "FROM "
				+ "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A, "
				+ "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B WHERE A.TABLE_SCHEMA LIKE ? "
				+ "AND A.CONSTRAINT_NAME=B.CONSTRAINT_NAME AND A.TABLE_NAME=? "
				+ "AND "
				+ "B.TABLE_NAME=? AND A.REFERENCED_TABLE_SCHEMA IS NOT NULL "
				+ " ORDER BY "
				+ "A.REFERENCED_TABLE_SCHEMA, A.REFERENCED_TABLE_NAME, "
				+ "A.ORDINAL_POSITION";

		PreparedStatement pStmt = null;

		try {
			pStmt = prepareMetaDataSafeStatement(sql);
			
			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, table);
			pStmt.setString(3, table);

			ResultSet rs = executeMetadataQuery(pStmt);

			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "PKTABLE_CAT", Types.CHAR, 255),
					new Field("", "PKTABLE_SCHEM", Types.CHAR, 0),
					new Field("", "PKTABLE_NAME", Types.CHAR, 255),
					new Field("", "PKCOLUMN_NAME", Types.CHAR, 32),
					new Field("", "FKTABLE_CAT", Types.CHAR, 255),
					new Field("", "FKTABLE_SCHEM", Types.CHAR, 0),
					new Field("", "FKTABLE_NAME", Types.CHAR, 255),
					new Field("", "FKCOLUMN_NAME", Types.CHAR, 32),
					new Field("", "KEY_SEQ", Types.SMALLINT, 2),
					new Field("", "UPDATE_RULE", Types.SMALLINT, 2),
					new Field("", "DELETE_RULE", Types.SMALLINT, 2),
					new Field("", "FK_NAME", Types.CHAR, 255),
					new Field("", "PK_NAME", Types.CHAR, 0),
					new Field("", "DEFERRABILITY", Types.INTEGER, 2) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of a table's indices and statistics. They are ordered
	 * by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
	 * <P>
	 * Each index column description has the following columns:
	 * <OL>
	 * <li> <B>TABLE_CAT</B> String => table catalog (may be null) </li>
	 * <li> <B>TABLE_SCHEM</B> String => table schema (may be null) </li>
	 * <li> <B>TABLE_NAME</B> String => table name </li>
	 * <li> <B>NON_UNIQUE</B> boolean => Can index values be non-unique? false
	 * when TYPE is tableIndexStatistic </li>
	 * <li> <B>INDEX_QUALIFIER</B> String => index catalog (may be null); null
	 * when TYPE is tableIndexStatistic </li>
	 * <li> <B>INDEX_NAME</B> String => index name; null when TYPE is
	 * tableIndexStatistic </li>
	 * <li> <B>TYPE</B> short => index type:
	 * <UL>
	 * <li> tableIndexStatistic - this identifies table statistics that are
	 * returned in conjuction with a table's index descriptions </li>
	 * <li> tableIndexClustered - this is a clustered index </li>
	 * <li> tableIndexHashed - this is a hashed index </li>
	 * <li> tableIndexOther - this is some other style of index </li>
	 * </ul>
	 * </li>
	 * <li> <B>ORDINAL_POSITION</B> short => column sequence number within
	 * index; zero when TYPE is tableIndexStatistic </li>
	 * <li> <B>COLUMN_NAME</B> String => column name; null when TYPE is
	 * tableIndexStatistic </li>
	 * <li> <B>ASC_OR_DESC</B> String => column sort sequence, "A" =>
	 * ascending, "D" => descending, may be null if sort sequence is not
	 * supported; null when TYPE is tableIndexStatistic </li>
	 * <li> <B>CARDINALITY</B> int => When TYPE is tableIndexStatisic then this
	 * is the number of rows in the table; otherwise it is the number of unique
	 * values in the index. </li>
	 * <li> <B>PAGES</B> int => When TYPE is tableIndexStatisic then this is
	 * the number of pages used for the table, otherwise it is the number of
	 * pages used for the current index. </li>
	 * <li> <B>FILTER_CONDITION</B> String => Filter condition, if any. (may be
	 * null) </li>
	 * </ol>
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schema
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param table
	 *            a table name
	 * @param unique
	 *            when true, return only indices for unique values; when false,
	 *            return indices regardless of whether unique or not
	 * @param approximate
	 *            when true, result is allowed to reflect approximate or out of
	 *            data values; when false, results are requested to be accurate
	 * @return ResultSet each row is an index column description
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	public ResultSet getIndexInfo(String catalog, String schema, String table,
			boolean unique, boolean approximate) throws SQLException {
		StringBuffer sqlBuf = new StringBuffer("SELECT "
				+ "TABLE_SCHEMA AS TABLE_CAT, " + "NULL AS TABLE_SCHEM,"
				+ "TABLE_NAME," + "NON_UNIQUE,"
				+ "TABLE_SCHEMA AS INDEX_QUALIFIER," + "INDEX_NAME,"
				+ tableIndexOther + " AS TYPE,"
				+ "SEQ_IN_INDEX AS ORDINAL_POSITION," + "COLUMN_NAME,"
				+ "COLLATION AS ASC_OR_DESC," + "CARDINALITY,"
				+ "NULL AS PAGES," + "NULL AS FILTER_CONDITION "
				+ "FROM INFORMATION_SCHEMA.STATISTICS WHERE "
				+ "TABLE_SCHEMA LIKE ? AND " + "TABLE_NAME LIKE ?");

		if (unique) {
			sqlBuf.append(" AND NON_UNIQUE=0 ");
		}

		sqlBuf.append("ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX");

		PreparedStatement pStmt = null;

		try {
			if (catalog == null) {
				if (this.conn.getNullCatalogMeansCurrent()) {
					catalog = this.database;
				}
			}
			
			pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());

			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, table);

			ResultSet rs = executeMetadataQuery(pStmt);

			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "TABLE_CAT", Types.CHAR, 255),
					new Field("", "TABLE_SCHEM", Types.CHAR, 0),
					new Field("", "TABLE_NAME", Types.CHAR, 255),
					new Field("", "NON_UNIQUE", Types.CHAR, 4),
					new Field("", "INDEX_QUALIFIER", Types.CHAR, 1),
					new Field("", "INDEX_NAME", Types.CHAR, 32),
					new Field("", "TYPE", Types.CHAR, 32),
					new Field("", "ORDINAL_POSITION", Types.SMALLINT, 5),
					new Field("", "COLUMN_NAME", Types.CHAR, 32),
					new Field("", "ASC_OR_DESC", Types.CHAR, 1),
					new Field("", "CARDINALITY", Types.INTEGER, 10),
					new Field("", "PAGES", Types.INTEGER, 10),
					new Field("", "FILTER_CONDITION", Types.CHAR, 32) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of a table's primary key columns. They are ordered by
	 * COLUMN_NAME.
	 * <P>
	 * Each column description has the following columns:
	 * <OL>
	 * <li> <B>TABLE_CAT</B> String => table catalog (may be null) </li>
	 * <li> <B>TABLE_SCHEM</B> String => table schema (may be null) </li>
	 * <li> <B>TABLE_NAME</B> String => table name </li>
	 * <li> <B>COLUMN_NAME</B> String => column name </li>
	 * <li> <B>KEY_SEQ</B> short => sequence number within primary key </li>
	 * <li> <B>PK_NAME</B> String => primary key name (may be null) </li>
	 * </ol>
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schema
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param table
	 *            a table name
	 * @return ResultSet each row is a primary key column description
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	public java.sql.ResultSet getPrimaryKeys(String catalog, String schema,
			String table) throws SQLException {

		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				catalog = this.database;
			}
		}

		if (table == null) {
			throw SQLError.createSQLException("Table not specified.",
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
		}

		String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, "
				+ "COLUMN_NAME, SEQ_IN_INDEX AS KEY_SEQ, 'PRIMARY' AS PK_NAME FROM INFORMATION_SCHEMA.STATISTICS "
				+ "WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND "
				+ "INDEX_NAME='PRIMARY' ORDER BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX";

		PreparedStatement pStmt = null;

		try {
			pStmt = prepareMetaDataSafeStatement(sql);

			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, table);

			ResultSet rs = executeMetadataQuery(pStmt);
			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "TABLE_CAT", Types.CHAR, 255),
					new Field("", "TABLE_SCHEM", Types.CHAR, 0),
					new Field("", "TABLE_NAME", Types.CHAR, 255),
					new Field("", "COLUMN_NAME", Types.CHAR, 32),
					new Field("", "KEY_SEQ", Types.SMALLINT, 5),
					new Field("", "PK_NAME", Types.CHAR, 32) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of stored procedures available in a catalog.
	 * <P>
	 * Only procedure descriptions matching the schema and procedure name
	 * criteria are returned. They are ordered by PROCEDURE_SCHEM, and
	 * PROCEDURE_NAME.
	 * </p>
	 * <P>
	 * Each procedure description has the the following columns:
	 * <OL>
	 * <li> <B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
	 * </li>
	 * <li> <B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
	 * </li>
	 * <li> <B>PROCEDURE_NAME</B> String => procedure name </li>
	 * <li> reserved for future use </li>
	 * <li> reserved for future use </li>
	 * <li> reserved for future use </li>
	 * <li> <B>REMARKS</B> String => explanatory comment on the procedure </li>
	 * <li> <B>PROCEDURE_TYPE</B> short => kind of procedure:
	 * <UL>
	 * <li> procedureResultUnknown - May return a result </li>
	 * <li> procedureNoResult - Does not return a result </li>
	 * <li> procedureReturnsResult - Returns a result </li>
	 * </ul>
	 * </li>
	 * </ol>
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schemaPattern
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param procedureNamePattern
	 *            a procedure name pattern
	 * @return ResultSet each row is a procedure description
	 * @throws SQLException
	 *             if a database access error occurs
	 * @see #getSearchStringEscape
	 */
	public ResultSet getProcedures(String catalog, String schemaPattern,
			String procedureNamePattern) throws SQLException {

		if ((procedureNamePattern == null)
				|| (procedureNamePattern.length() == 0)) {
			if (this.conn.getNullNamePatternMatchesAll()) {
				procedureNamePattern = "%";
			} else {
				throw SQLError.createSQLException(
						"Procedure name pattern can not be NULL or empty.",
						SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
			}
		}

		String db = null;

		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				db = this.database;
			}
		}

		String sql = "SELECT ROUTINE_SCHEMA AS PROCEDURE_CAT, "
				+ "NULL AS PROCEDURE_SCHEM, "
				+ "ROUTINE_NAME AS PROCEDURE_NAME, " + "NULL AS RESERVED_1, "
				+ "NULL AS RESERVED_2, " + "NULL AS RESERVED_3, "
				+ "ROUTINE_COMMENT AS REMARKS, "
				+ "CASE WHEN ROUTINE_TYPE = 'PROCEDURE' THEN "
				+ procedureNoResult + " WHEN ROUTINE_TYPE='FUNCTION' THEN "
				+ procedureReturnsResult + " ELSE " + procedureResultUnknown
				+ " END AS PROCEDURE_TYPE "
				+ "FROM INFORMATION_SCHEMA.ROUTINES WHERE "
				+ "ROUTINE_SCHEMA LIKE ? AND ROUTINE_NAME LIKE ? "
				+ "ORDER BY ROUTINE_SCHEMA, ROUTINE_NAME";

		PreparedStatement pStmt = null;

		try {
			pStmt = prepareMetaDataSafeStatement(sql);
			
			if (db != null) {
				pStmt.setString(1, db);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, procedureNamePattern);

			ResultSet rs = executeMetadataQuery(pStmt);
			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "PROCEDURE_CAT", Types.CHAR, 0),
					new Field("", "PROCEDURE_SCHEM", Types.CHAR, 0),
					new Field("", "PROCEDURE_NAME", Types.CHAR, 0),
					new Field("", "reserved1", Types.CHAR, 0),
					new Field("", "reserved2", Types.CHAR, 0),
					new Field("", "reserved3", Types.CHAR, 0),
					new Field("", "REMARKS", Types.CHAR, 0),
					new Field("", "PROCEDURE_TYPE", Types.SMALLINT, 0) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	/**
	 * Get a description of tables available in a catalog.
	 * <P>
	 * Only table descriptions matching the catalog, schema, table name and type
	 * criteria are returned. They are ordered by TABLE_TYPE, TABLE_SCHEM and
	 * TABLE_NAME.
	 * </p>
	 * <P>
	 * Each table description has the following columns:
	 * <OL>
	 * <li> <B>TABLE_CAT</B> String => table catalog (may be null) </li>
	 * <li> <B>TABLE_SCHEM</B> String => table schema (may be null) </li>
	 * <li> <B>TABLE_NAME</B> String => table name </li>
	 * <li> <B>TABLE_TYPE</B> String => table type. Typical types are "TABLE",
	 * "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
	 * "SYNONYM". </li>
	 * <li> <B>REMARKS</B> String => explanatory comment on the table </li>
	 * </ol>
	 * </p>
	 * <P>
	 * <B>Note:</B> Some databases may not return information for all tables.
	 * </p>
	 * 
	 * @param catalog
	 *            a catalog name; "" retrieves those without a catalog
	 * @param schemaPattern
	 *            a schema name pattern; "" retrieves those without a schema
	 * @param tableNamePattern
	 *            a table name pattern
	 * @param types
	 *            a list of table types to include; null returns all types
	 * @return ResultSet each row is a table description
	 * @throws SQLException
	 *             DOCUMENT ME!
	 * @see #getSearchStringEscape
	 */
	public ResultSet getTables(String catalog, String schemaPattern,
			String tableNamePattern, String[] types) throws SQLException {
		if (catalog == null) {
			if (this.conn.getNullCatalogMeansCurrent()) {
				catalog = this.database;
			}
		}

		if (tableNamePattern == null) {
			if (this.conn.getNullNamePatternMatchesAll()) {
				tableNamePattern = "%";
			} else {
				throw SQLError.createSQLException(
						"Table name pattern can not be NULL or empty.",
						SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
			}
		}

		PreparedStatement pStmt = null;

		String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, "
				+ "NULL AS TABLE_SCHEM, TABLE_NAME, "
				+ "CASE WHEN TABLE_TYPE='BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, "
				+ "TABLE_COMMENT AS REMARKS "
				+ "FROM INFORMATION_SCHEMA.TABLES WHERE "
				+ "TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND TABLE_TYPE IN (?,?,?) "
				+ "ORDER BY TABLE_TYPE, TABLE_SCHEMA, TABLE_NAME";
		try {
			pStmt = prepareMetaDataSafeStatement(sql);
			
			if (catalog != null) {
				pStmt.setString(1, catalog);
			} else {
				pStmt.setString(1, "%");
			}
			
			pStmt.setString(2, tableNamePattern);

			// This overloading of IN (...) allows us to cache this
			// prepared statement
			if (types == null || types.length == 0) {
				pStmt.setString(3, "BASE TABLE");
				pStmt.setString(4, "VIEW");
				pStmt.setString(5, "TEMPORARY");
			} else {
				pStmt.setNull(3, Types.VARCHAR);
				pStmt.setNull(4, Types.VARCHAR);
				pStmt.setNull(5, Types.VARCHAR);

				for (int i = 0; i < types.length; i++) {
					if ("TABLE".equalsIgnoreCase(types[i])) {
						pStmt.setString(3, "BASE TABLE");
					}

					if ("VIEW".equalsIgnoreCase(types[i])) {
						pStmt.setString(4, "VIEW");
					}

					if ("LOCAL TEMPORARY".equalsIgnoreCase(types[i])) {
						pStmt.setString(5, "TEMPORARY");
					}
				}
			}

			ResultSet rs = executeMetadataQuery(pStmt);

			((com.mysql.jdbc.ResultSet) rs).redefineFieldsForDBMD(new Field[] {
					new Field("", "TABLE_CAT", java.sql.Types.VARCHAR,
							(catalog == null) ? 0 : catalog.length()),
					new Field("", "TABLE_SCHEM", java.sql.Types.VARCHAR, 0),
					new Field("", "TABLE_NAME", java.sql.Types.VARCHAR, 255),
					new Field("", "TABLE_TYPE", java.sql.Types.VARCHAR, 5),
					new Field("", "REMARKS", java.sql.Types.VARCHAR, 0) });

			return rs;
		} finally {
			if (pStmt != null) {
				pStmt.close();
			}
		}
	}

	private PreparedStatement prepareMetaDataSafeStatement(String sql)
			throws SQLException {
		// Can't use server-side here as we coerce a lot of types to match
		// the spec.
		PreparedStatement pStmt = this.conn.clientPrepareStatement(sql);

		if (pStmt.getMaxRows() != 0) {
			pStmt.setMaxRows(0);
		}

		pStmt.setHoldResultsOpenOverClose(true);

		return pStmt;
	}
}
