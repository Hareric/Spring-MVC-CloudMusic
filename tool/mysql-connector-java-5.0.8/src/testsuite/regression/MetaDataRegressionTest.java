/*
 Copyright (C) 2002-2007 MySQL AB

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
package testsuite.regression;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Properties;

import testsuite.BaseTestCase;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.Field;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.SQLError;

/**
 * Regression tests for DatabaseMetaData
 * 
 * @author Mark Matthews
 * @version $Id: MetaDataRegressionTest.java,v 1.1.2.1 2005/05/13 18:58:38
 *          mmatthews Exp $
 */
public class MetaDataRegressionTest extends BaseTestCase {
	/**
	 * Creates a new MetaDataRegressionTest.
	 * 
	 * @param name
	 *            the name of the test
	 */
	public MetaDataRegressionTest(String name) {
		super(name);
	}

	/**
	 * Runs all test cases in this test suite
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(MetaDataRegressionTest.class);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             ...
	 */
	public void testBug2607() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2607");
			this.stmt
					.executeUpdate("CREATE TABLE testBug2607 (field1 INT PRIMARY KEY)");

			this.rs = this.stmt.executeQuery("SELECT field1 FROM testBug2607");

			ResultSetMetaData rsmd = this.rs.getMetaData();

			assertTrue(!rsmd.isAutoIncrement(1));
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2607");
		}
	}

	/**
	 * Tests fix for BUG#2852, where RSMD is not returning correct (or matching)
	 * types for TINYINT and SMALLINT.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug2852() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2852");
			this.stmt
					.executeUpdate("CREATE TABLE testBug2852 (field1 TINYINT, field2 SMALLINT)");
			this.stmt.executeUpdate("INSERT INTO testBug2852 VALUES (1,1)");

			this.rs = this.stmt.executeQuery("SELECT * from testBug2852");

			assertTrue(this.rs.next());

			ResultSetMetaData rsmd = this.rs.getMetaData();

			assertTrue(rsmd.getColumnClassName(1).equals(
					this.rs.getObject(1).getClass().getName()));
			assertTrue("java.lang.Integer".equals(rsmd.getColumnClassName(1)));

			assertTrue(rsmd.getColumnClassName(2).equals(
					this.rs.getObject(2).getClass().getName()));
			assertTrue("java.lang.Integer".equals(rsmd.getColumnClassName(2)));
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2852");
		}
	}

	/**
	 * Tests fix for BUG#2855, where RSMD is not returning correct (or matching)
	 * types for FLOAT.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug2855() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2855");
			this.stmt.executeUpdate("CREATE TABLE testBug2855 (field1 FLOAT)");
			this.stmt.executeUpdate("INSERT INTO testBug2855 VALUES (1)");

			this.rs = this.stmt.executeQuery("SELECT * from testBug2855");

			assertTrue(this.rs.next());

			ResultSetMetaData rsmd = this.rs.getMetaData();

			assertTrue(rsmd.getColumnClassName(1).equals(
					this.rs.getObject(1).getClass().getName()));
			assertTrue("java.lang.Float".equals(rsmd.getColumnClassName(1)));
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2855");
		}
	}

	/**
	 * Tests fix for BUG#3570 -- inconsistent reporting of column type
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void testBug3570() throws Exception {
		String createTableQuery = " CREATE TABLE testBug3570(field_tinyint TINYINT"
				+ ",field_smallint SMALLINT"
				+ ",field_mediumint MEDIUMINT"
				+ ",field_int INT"
				+ ",field_integer INTEGER"
				+ ",field_bigint BIGINT"
				+ ",field_real REAL"
				+ ",field_float FLOAT"
				+ ",field_decimal DECIMAL"
				+ ",field_numeric NUMERIC"
				+ ",field_double DOUBLE"
				+ ",field_char CHAR(3)"
				+ ",field_varchar VARCHAR(255)"
				+ ",field_date DATE"
				+ ",field_time TIME"
				+ ",field_year YEAR"
				+ ",field_timestamp TIMESTAMP"
				+ ",field_datetime DATETIME"
				+ ",field_tinyblob TINYBLOB"
				+ ",field_blob BLOB"
				+ ",field_mediumblob MEDIUMBLOB"
				+ ",field_longblob LONGBLOB"
				+ ",field_tinytext TINYTEXT"
				+ ",field_text TEXT"
				+ ",field_mediumtext MEDIUMTEXT"
				+ ",field_longtext LONGTEXT"
				+ ",field_enum ENUM('1','2','3')"
				+ ",field_set SET('1','2','3'))";

		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3570");
			this.stmt.executeUpdate(createTableQuery);

			ResultSet dbmdRs = this.conn.getMetaData().getColumns(
					this.conn.getCatalog(), null, "testBug3570", "%");

			this.rs = this.stmt.executeQuery("SELECT * FROM testBug3570");

			ResultSetMetaData rsmd = this.rs.getMetaData();

			while (dbmdRs.next()) {
				String columnName = dbmdRs.getString(4);
				int typeFromGetColumns = dbmdRs.getInt(5);
				int typeFromRSMD = rsmd.getColumnType(this.rs
						.findColumn(columnName));

				//
				// TODO: Server needs to send these types correctly....
				//
				if (!"field_tinyblob".equals(columnName)
						&& !"field_tinytext".equals(columnName)) {
					assertTrue(columnName + " -> type from DBMD.getColumns("
							+ typeFromGetColumns
							+ ") != type from RSMD.getColumnType("
							+ typeFromRSMD + ")",
							typeFromGetColumns == typeFromRSMD);
				}
			}
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3570");
		}
	}

	/**
	 * Tests char/varchar bug
	 * 
	 * @throws Exception
	 *             if any errors occur
	 */
	public void testCharVarchar() throws Exception {
		try {
			this.stmt.execute("DROP TABLE IF EXISTS charVarCharTest");
			this.stmt.execute("CREATE TABLE charVarCharTest ("
					+ "  TableName VARCHAR(64)," + "  FieldName VARCHAR(64),"
					+ "  NextCounter INTEGER);");

			String query = "SELECT TableName, FieldName, NextCounter FROM charVarCharTest";
			this.rs = this.stmt.executeQuery(query);

			ResultSetMetaData rsmeta = this.rs.getMetaData();

			assertTrue(rsmeta.getColumnTypeName(1).equalsIgnoreCase("VARCHAR"));

			// is "CHAR", expected "VARCHAR"
			assertTrue(rsmeta.getColumnType(1) == 12);

			// is 1 (java.sql.Types.CHAR), expected 12 (java.sql.Types.VARCHAR)
		} finally {
			this.stmt.execute("DROP TABLE IF EXISTS charVarCharTest");
		}
	}

	/**
	 * Tests fix for BUG#1673, where DatabaseMetaData.getColumns() is not
	 * returning correct column ordinal info for non '%' column name patterns.
	 * 
	 * @throws Exception
	 *             if the test fails for any reason
	 */
	public void testFixForBug1673() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1673");
			this.stmt
					.executeUpdate("CREATE TABLE testBug1673 (field_1 INT, field_2 INT)");

			DatabaseMetaData dbmd = this.conn.getMetaData();

			int ordinalPosOfCol2Full = 0;

			this.rs = dbmd.getColumns(this.conn.getCatalog(), null,
					"testBug1673", null);

			while (this.rs.next()) {
				if (this.rs.getString(4).equals("field_2")) {
					ordinalPosOfCol2Full = this.rs.getInt(17);
				}
			}

			int ordinalPosOfCol2Scoped = 0;

			this.rs = dbmd.getColumns(this.conn.getCatalog(), null,
					"testBug1673", "field_2");

			while (this.rs.next()) {
				if (this.rs.getString(4).equals("field_2")) {
					ordinalPosOfCol2Scoped = this.rs.getInt(17);
				}
			}

			assertTrue("Ordinal position in full column list of '"
					+ ordinalPosOfCol2Full
					+ "' != ordinal position in pattern search, '"
					+ ordinalPosOfCol2Scoped + "'.",
					(ordinalPosOfCol2Full != 0)
							&& (ordinalPosOfCol2Scoped != 0)
							&& (ordinalPosOfCol2Scoped == ordinalPosOfCol2Full));
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1673");
		}
	}

	/**
	 * Tests bug reported by OpenOffice team with getColumns and LONGBLOB
	 * 
	 * @throws Exception
	 *             if any errors occur
	 */
	public void testGetColumns() throws Exception {
		try {
			this.stmt
					.execute("CREATE TABLE IF NOT EXISTS longblob_regress(field_1 longblob)");

			DatabaseMetaData dbmd = this.conn.getMetaData();
			ResultSet dbmdRs = null;

			try {
				dbmdRs = dbmd.getColumns("", "", "longblob_regress", "%");

				while (dbmdRs.next()) {
					dbmdRs.getInt(7);
				}
			} finally {
				if (dbmdRs != null) {
					try {
						dbmdRs.close();
					} catch (SQLException ex) {
						;
					}
				}
			}
		} finally {
			this.stmt.execute("DROP TABLE IF EXISTS longblob_regress");
		}
	}

	/**
	 * Tests fix for Bug#
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void testGetColumnsBug1099() throws Exception {
		try {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testGetColumnsBug1099");

			DatabaseMetaData dbmd = this.conn.getMetaData();

			this.rs = dbmd.getTypeInfo();

			StringBuffer types = new StringBuffer();

			HashMap alreadyDoneTypes = new HashMap();

			while (this.rs.next()) {
				String typeName = this.rs.getString("TYPE_NAME");
				String createParams = this.rs.getString("CREATE_PARAMS");

				if ((typeName.indexOf("BINARY") == -1)
						&& !typeName.equals("LONG VARCHAR")) {
					if (!alreadyDoneTypes.containsKey(typeName)) {
						alreadyDoneTypes.put(typeName, null);

						if (types.length() != 0) {
							types.append(", \n");
						}

						int typeNameLength = typeName.length();
						StringBuffer safeTypeName = new StringBuffer(
								typeNameLength);

						for (int i = 0; i < typeNameLength; i++) {
							char c = typeName.charAt(i);

							if (Character.isWhitespace(c)) {
								safeTypeName.append("_");
							} else {
								safeTypeName.append(c);
							}
						}

						types.append(safeTypeName.toString());
						types.append("Column ");
						types.append(typeName);

						if (typeName.indexOf("CHAR") != -1) {
							types.append(" (1)");
						} else if (typeName.equalsIgnoreCase("enum")
								|| typeName.equalsIgnoreCase("set")) {
							types.append("('a', 'b', 'c')");
						}
					}
				}
			}

			this.stmt.executeUpdate("CREATE TABLE testGetColumnsBug1099("
					+ types.toString() + ")");

			dbmd.getColumns(null, this.conn.getCatalog(),
					"testGetColumnsBug1099", "%");
		} finally {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testGetColumnsBug1099");
		}
	}

	/**
	 * Tests whether or not unsigned columns are reported correctly in
	 * DBMD.getColumns
	 * 
	 * @throws Exception
	 */
	public void testGetColumnsUnsigned() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testGetUnsignedCols");
			this.stmt
					.executeUpdate("CREATE TABLE testGetUnsignedCols (field1 BIGINT, field2 BIGINT UNSIGNED)");

			DatabaseMetaData dbmd = this.conn.getMetaData();

			this.rs = dbmd.getColumns(this.conn.getCatalog(), null,
					"testGetUnsignedCols", "%");

			assertTrue(this.rs.next());
			// This row doesn't have 'unsigned' attribute
			assertTrue(this.rs.next());
			assertTrue(this.rs.getString(6).toLowerCase().indexOf("unsigned") != -1);
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testGetUnsignedCols");
		}
	}

	/**
	 * Tests whether bogus parameters break Driver.getPropertyInfo().
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testGetPropertyInfo() throws Exception {
		new Driver().getPropertyInfo("", null);
	}

	/**
	 * Tests whether ResultSetMetaData returns correct info for CHAR/VARCHAR
	 * columns.
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testIsCaseSensitive() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testIsCaseSensitive");
			this.stmt
					.executeUpdate("CREATE TABLE testIsCaseSensitive (bin_char CHAR(1) BINARY, bin_varchar VARCHAR(64) BINARY, ci_char CHAR(1), ci_varchar VARCHAR(64))");
			this.rs = this.stmt
					.executeQuery("SELECT bin_char, bin_varchar, ci_char, ci_varchar FROM testIsCaseSensitive");

			ResultSetMetaData rsmd = this.rs.getMetaData();
			assertTrue(rsmd.isCaseSensitive(1));
			assertTrue(rsmd.isCaseSensitive(2));
			assertTrue(!rsmd.isCaseSensitive(3));
			assertTrue(!rsmd.isCaseSensitive(4));
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testIsCaseSensitive");
		}

		if (versionMeetsMinimum(4, 1)) {
			try {
				this.stmt
						.executeUpdate("DROP TABLE IF EXISTS testIsCaseSensitiveCs");
				this.stmt
						.executeUpdate("CREATE TABLE testIsCaseSensitiveCs ("
								+ "bin_char CHAR(1) CHARACTER SET latin1 COLLATE latin1_general_cs,"
								+ "bin_varchar VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_cs,"
								+ "ci_char CHAR(1) CHARACTER SET latin1 COLLATE latin1_general_ci,"
								+ "ci_varchar VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci, "
								+ "bin_tinytext TINYTEXT CHARACTER SET latin1 COLLATE latin1_general_cs,"
								+ "bin_text TEXT CHARACTER SET latin1 COLLATE latin1_general_cs,"
								+ "bin_med_text MEDIUMTEXT CHARACTER SET latin1 COLLATE latin1_general_cs,"
								+ "bin_long_text LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_cs,"
								+ "ci_tinytext TINYTEXT CHARACTER SET latin1 COLLATE latin1_general_ci,"
								+ "ci_text TEXT CHARACTER SET latin1 COLLATE latin1_general_ci,"
								+ "ci_med_text MEDIUMTEXT CHARACTER SET latin1 COLLATE latin1_general_ci,"
								+ "ci_long_text LONGTEXT CHARACTER SET latin1 COLLATE latin1_general_ci)");

				this.rs = this.stmt
						.executeQuery("SELECT bin_char, bin_varchar, ci_char, ci_varchar, bin_tinytext, bin_text, bin_med_text, bin_long_text, ci_tinytext, ci_text, ci_med_text, ci_long_text FROM testIsCaseSensitiveCs");

				ResultSetMetaData rsmd = this.rs.getMetaData();
				assertTrue(rsmd.isCaseSensitive(1));
				assertTrue(rsmd.isCaseSensitive(2));
				assertTrue(!rsmd.isCaseSensitive(3));
				assertTrue(!rsmd.isCaseSensitive(4));

				assertTrue(rsmd.isCaseSensitive(5));
				assertTrue(rsmd.isCaseSensitive(6));
				assertTrue(rsmd.isCaseSensitive(7));
				assertTrue(rsmd.isCaseSensitive(8));

				assertTrue(!rsmd.isCaseSensitive(9));
				assertTrue(!rsmd.isCaseSensitive(10));
				assertTrue(!rsmd.isCaseSensitive(11));
				assertTrue(!rsmd.isCaseSensitive(12));
			} finally {
				this.stmt
						.executeUpdate("DROP TABLE IF EXISTS testIsCaseSensitiveCs");
			}
		}
	}

	/**
	 * Tests whether or not DatabaseMetaData.getColumns() returns the correct
	 * java.sql.Types info.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testLongText() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testLongText");
			this.stmt
					.executeUpdate("CREATE TABLE testLongText (field1 LONGTEXT)");

			this.rs = this.conn.getMetaData().getColumns(
					this.conn.getCatalog(), null, "testLongText", "%");

			assertTrue(this.rs.next());

			assertTrue(this.rs.getInt("DATA_TYPE") == java.sql.Types.LONGVARCHAR);
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testLongText");
		}
	}

	/**
	 * Tests for types being returned correctly
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testTypes() throws Exception {
		try {
			this.stmt.execute("DROP TABLE IF EXISTS typesRegressTest");
			this.stmt.execute("CREATE TABLE typesRegressTest ("
					+ "varcharField VARCHAR(32)," + "charField CHAR(2),"
					+ "enumField ENUM('1','2'),"
					+ "setField  SET('1','2','3')," + "tinyblobField TINYBLOB,"
					+ "mediumBlobField MEDIUMBLOB," + "longblobField LONGBLOB,"
					+ "blobField BLOB)");

			this.rs = this.stmt.executeQuery("SELECT * from typesRegressTest");

			ResultSetMetaData rsmd = this.rs.getMetaData();

			int numCols = rsmd.getColumnCount();

			for (int i = 0; i < numCols; i++) {
				String columnName = rsmd.getColumnName(i + 1);
				String columnTypeName = rsmd.getColumnTypeName(i + 1);
				System.out.println(columnName + " -> " + columnTypeName);
			}
		} finally {
			this.stmt.execute("DROP TABLE IF EXISTS typesRegressTest");
		}
	}

	/**
	 * Tests fix for BUG#4742, 'DOUBLE' mapped twice in getTypeInfo().
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug4742() throws Exception {
		HashMap clashMap = new HashMap();

		this.rs = this.conn.getMetaData().getTypeInfo();

		while (this.rs.next()) {
			String name = this.rs.getString(1);
			assertTrue("Type represented twice in type info, '" + name + "'.",
					!clashMap.containsKey(name));
			clashMap.put(name, name);
		}
	}

	/**
	 * Tests fix for BUG#4138, getColumns() returns incorrect JDBC type for
	 * unsigned columns.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug4138() throws Exception {
		try {
			String[] typesToTest = new String[] { "TINYINT", "SMALLINT",
					"MEDIUMINT", "INT", "BIGINT", "FLOAT", "DOUBLE",
					"DECIMAL" };

			short[] jdbcMapping = new short[] { Types.TINYINT, Types.SMALLINT,
					Types.INTEGER, Types.INTEGER, Types.BIGINT, Types.REAL,
					Types.DOUBLE, Types.DECIMAL };

			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug4138");

			StringBuffer createBuf = new StringBuffer();

			createBuf.append("CREATE TABLE testBug4138 (");

			boolean firstColumn = true;

			for (int i = 0; i < typesToTest.length; i++) {
				if (!firstColumn) {
					createBuf.append(", ");
				} else {
					firstColumn = false;
				}

				createBuf.append("field");
				createBuf.append((i + 1));
				createBuf.append(" ");
				createBuf.append(typesToTest[i]);
				createBuf.append(" UNSIGNED");
			}
			createBuf.append(")");
			this.stmt.executeUpdate(createBuf.toString());

			DatabaseMetaData dbmd = this.conn.getMetaData();
			this.rs = dbmd.getColumns(this.conn.getCatalog(), null,
					"testBug4138", "field%");

			assertTrue(this.rs.next());

			for (int i = 0; i < typesToTest.length; i++) {
				assertTrue(
						"JDBC Data Type of "
								+ this.rs.getShort("DATA_TYPE")
								+ " for MySQL type '"
								+ this.rs.getString("TYPE_NAME")
								+ "' from 'DATA_TYPE' column does not match expected value of "
								+ jdbcMapping[i] + ".",
						jdbcMapping[i] == this.rs.getShort("DATA_TYPE"));
				this.rs.next();
			}

			this.rs.close();

			StringBuffer queryBuf = new StringBuffer("SELECT ");
			firstColumn = true;

			for (int i = 0; i < typesToTest.length; i++) {
				if (!firstColumn) {
					queryBuf.append(", ");
				} else {
					firstColumn = false;
				}

				queryBuf.append("field");
				queryBuf.append((i + 1));
			}

			queryBuf.append(" FROM testBug4138");

			this.rs = this.stmt.executeQuery(queryBuf.toString());

			ResultSetMetaData rsmd = this.rs.getMetaData();

			for (int i = 0; i < typesToTest.length; i++) {

				assertTrue(jdbcMapping[i] == rsmd.getColumnType(i + 1));
				String desiredTypeName = typesToTest[i] + " unsigned";

				assertTrue(rsmd.getColumnTypeName((i + 1)) + " != "
						+ desiredTypeName, desiredTypeName
						.equalsIgnoreCase(rsmd.getColumnTypeName(i + 1)));
			}
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug4138");
		}
	}

	/**
	 * Here for housekeeping only, the test is actually in testBug4138().
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug4860() throws Exception {
		testBug4138();
	}

	/**
	 * Tests fix for BUG#4880 - RSMD.getPrecision() returns '0' for non-numeric
	 * types.
	 * 
	 * Why-oh-why is this not in the spec, nor the api-docs, but in some
	 * 'optional' book, _and_ it is a variance from both ODBC and the ANSI SQL
	 * standard :p
	 * 
	 * (from the CTS testsuite)....
	 * 
	 * The getPrecision(int colindex) method returns an integer value
	 * representing the number of decimal digits for number types,maximum length
	 * in characters for character types,maximum length in bytes for JDBC binary
	 * datatypes.
	 * 
	 * (See Section 27.3 of JDBC 2.0 API Reference & Tutorial 2nd edition)
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */

	public void testBug4880() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug4880");
			this.stmt
					.executeUpdate("CREATE TABLE testBug4880 (field1 VARCHAR(80), field2 TINYBLOB, field3 BLOB, field4 MEDIUMBLOB, field5 LONGBLOB)");
			this.rs = this.stmt
					.executeQuery("SELECT field1, field2, field3, field4, field5 FROM testBug4880");
			ResultSetMetaData rsmd = this.rs.getMetaData();

			assertEquals(80, rsmd.getPrecision(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
			assertEquals(80, rsmd.getColumnDisplaySize(1));

			assertEquals(255, rsmd.getPrecision(2));
			assertEquals(Types.VARBINARY, rsmd.getColumnType(2));
			assertTrue("TINYBLOB".equalsIgnoreCase(rsmd.getColumnTypeName(2)));
			assertEquals(255, rsmd.getColumnDisplaySize(2));

			assertEquals(65535, rsmd.getPrecision(3));
			assertEquals(Types.LONGVARBINARY, rsmd.getColumnType(3));
			assertTrue("BLOB".equalsIgnoreCase(rsmd.getColumnTypeName(3)));
			assertEquals(65535, rsmd.getColumnDisplaySize(3));

			assertEquals(16777215, rsmd.getPrecision(4));
			assertEquals(Types.LONGVARBINARY, rsmd.getColumnType(4));
			assertTrue("MEDIUMBLOB".equalsIgnoreCase(rsmd.getColumnTypeName(4)));
			assertEquals(16777215, rsmd.getColumnDisplaySize(4));

			if (versionMeetsMinimum(4, 1)) {
				// Server doesn't send us enough information to detect LONGBLOB
				// type
				assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(5));
				assertEquals(Types.LONGVARBINARY, rsmd.getColumnType(5));
				assertTrue("LONGBLOB".equalsIgnoreCase(rsmd
						.getColumnTypeName(5)));
				assertEquals(Integer.MAX_VALUE, rsmd.getColumnDisplaySize(5));
			}
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug4880");
		}
	}

	/**
	 * Tests fix for BUG#6399, ResultSetMetaData.getDisplaySize() is wrong for
	 * multi-byte charsets.
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testBug6399() throws Exception {
		if (versionMeetsMinimum(4, 1)) {
			try {
				this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug6399");
				this.stmt
						.executeUpdate("CREATE TABLE testBug6399 (field1 CHAR(3) CHARACTER SET UTF8, field2 CHAR(3) CHARACTER SET LATIN1, field3 CHAR(3) CHARACTER SET SJIS)");
				this.stmt
						.executeUpdate("INSERT INTO testBug6399 VALUES ('a', 'a', 'a')");

				this.rs = this.stmt
						.executeQuery("SELECT field1, field2, field3 FROM testBug6399");
				ResultSetMetaData rsmd = this.rs.getMetaData();

				assertTrue(3 == rsmd.getColumnDisplaySize(1));
				assertTrue(3 == rsmd.getColumnDisplaySize(2));
				assertTrue(3 == rsmd.getColumnDisplaySize(3));
			} finally {
				this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug6399");
			}
		}
	}

	/**
	 * Tests fix for BUG#7081, DatabaseMetaData.getIndexInfo() ignoring 'unique'
	 * parameters.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug7081() throws Exception {
		String tableName = "testBug7081";

		try {
			createTable(tableName, "(field1 INT, INDEX(field1))");

			DatabaseMetaData dbmd = this.conn.getMetaData();
			this.rs = dbmd.getIndexInfo(this.conn.getCatalog(), null,
					tableName, true, false);
			assertTrue(!this.rs.next()); // there should be no rows that meet
			// this requirement

			this.rs = dbmd.getIndexInfo(this.conn.getCatalog(), null,
					tableName, false, false);
			assertTrue(this.rs.next()); // there should be one row that meets
			// this requirement
			assertTrue(!this.rs.next());

		} finally {
			dropTable(tableName);
		}
	}

	/**
	 * Tests fix for BUG#7033 - PreparedStatements don't encode Big5 (and other
	 * multibyte) character sets correctly in static SQL strings.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug7033() throws Exception {
		if (false) { // disabled for now
			Connection big5Conn = null;
			Statement big5Stmt = null;
			PreparedStatement big5PrepStmt = null;

			String testString = "\u5957 \u9910";

			try {
				Properties props = new Properties();
				props.setProperty("useUnicode", "true");
				props.setProperty("characterEncoding", "Big5");

				big5Conn = getConnectionWithProps(props);
				big5Stmt = big5Conn.createStatement();

				byte[] foobar = testString.getBytes("Big5");
				System.out.println(foobar);

				this.rs = big5Stmt.executeQuery("select 1 as '\u5957 \u9910'");
				String retrString = this.rs.getMetaData().getColumnName(1);
				assertTrue(testString.equals(retrString));

				big5PrepStmt = big5Conn
						.prepareStatement("select 1 as '\u5957 \u9910'");
				this.rs = big5PrepStmt.executeQuery();
				retrString = this.rs.getMetaData().getColumnName(1);
				assertTrue(testString.equals(retrString));
			} finally {
				if (this.rs != null) {
					this.rs.close();
					this.rs = null;
				}

				if (big5Stmt != null) {
					big5Stmt.close();

				}

				if (big5PrepStmt != null) {
					big5PrepStmt.close();
				}

				if (big5Conn != null) {
					big5Conn.close();
				}
			}
		}
	}

	/**
	 * Tests fix for Bug#8812, DBMD.getIndexInfo() returning inverted values for
	 * 'NON_UNIQUE' column.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug8812() throws Exception {
		String tableName = "testBug8812";

		try {
			createTable(tableName,
					"(field1 INT, field2 INT, INDEX(field1), UNIQUE INDEX(field2))");

			DatabaseMetaData dbmd = this.conn.getMetaData();
			this.rs = dbmd.getIndexInfo(this.conn.getCatalog(), null,
					tableName, true, false);
			assertTrue(this.rs.next()); // there should be one row that meets
			// this requirement
			assertEquals(this.rs.getBoolean("NON_UNIQUE"), false);

			this.rs = dbmd.getIndexInfo(this.conn.getCatalog(), null,
					tableName, false, false);
			assertTrue(this.rs.next()); // there should be two rows that meets
			// this requirement
			assertEquals(this.rs.getBoolean("NON_UNIQUE"), false);
			assertTrue(this.rs.next());
			assertEquals(this.rs.getBoolean("NON_UNIQUE"), true);

		} finally {
			dropTable(tableName);
		}
	}

	/**
	 * Tests fix for BUG#8800 - supportsMixedCase*Identifiers() returns wrong
	 * value on servers running on case-sensitive filesystems.
	 */

	public void testBug8800() throws Exception {
		assertEquals(((com.mysql.jdbc.Connection) this.conn)
				.lowerCaseTableNames(), !this.conn.getMetaData()
				.supportsMixedCaseIdentifiers());
		assertEquals(((com.mysql.jdbc.Connection) this.conn)
				.lowerCaseTableNames(), !this.conn.getMetaData()
				.supportsMixedCaseQuotedIdentifiers());

	}

	/**
	 * Tests fix for BUG#8792 - DBMD.supportsResultSetConcurrency() not
	 * returning true for forward-only/read-only result sets (we obviously
	 * support this).
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug8792() throws Exception {
		DatabaseMetaData dbmd = this.conn.getMetaData();

		assertTrue(dbmd.supportsResultSetConcurrency(
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));

		assertTrue(dbmd.supportsResultSetConcurrency(
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE));

		assertTrue(dbmd.supportsResultSetConcurrency(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));

		assertTrue(dbmd.supportsResultSetConcurrency(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));

		assertTrue(!dbmd.supportsResultSetConcurrency(
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY));

		assertTrue(!dbmd.supportsResultSetConcurrency(
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE));

		// Check error conditions
		try {
			dbmd.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY,
					Integer.MIN_VALUE);
			fail("Exception should've been raised for bogus concurrency value");
		} catch (SQLException sqlEx) {
			assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
					.getSQLState()));
		}

		try {
			assertTrue(dbmd.supportsResultSetConcurrency(
					ResultSet.TYPE_SCROLL_INSENSITIVE, Integer.MIN_VALUE));
			fail("Exception should've been raised for bogus concurrency value");
		} catch (SQLException sqlEx) {
			assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
					.getSQLState()));
		}

		try {
			assertTrue(dbmd.supportsResultSetConcurrency(Integer.MIN_VALUE,
					Integer.MIN_VALUE));
			fail("Exception should've been raised for bogus concurrency value");
		} catch (SQLException sqlEx) {
			assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
					.getSQLState()));
		}
	}

	/**
	 * Tests fix for BUG#8803, 'DATA_TYPE' column from
	 * DBMD.getBestRowIdentifier() causes ArrayIndexOutOfBoundsException when
	 * accessed (and in fact, didn't return any value).
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug8803() throws Exception {
		String tableName = "testBug8803";
		createTable(tableName, "(field1 INT NOT NULL PRIMARY KEY)");
		DatabaseMetaData metadata = this.conn.getMetaData();
		try {
			this.rs = metadata.getBestRowIdentifier(this.conn.getCatalog(),
					null, tableName, DatabaseMetaData.bestRowNotPseudo, true);

			assertTrue(this.rs.next());

			this.rs.getInt("DATA_TYPE"); // **** Fails here *****
		} finally {
			if (this.rs != null) {
				this.rs.close();

				this.rs = null;
			}
		}

	}

	/**
	 * Tests fix for BUG#9320 - PreparedStatement.getMetaData() inserts blank
	 * row in database under certain conditions when not using server-side
	 * prepared statements.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug9320() throws Exception {
		createTable("testBug9320", "(field1 int)");

		testAbsenceOfMetadataForQuery("INSERT INTO testBug9320 VALUES (?)");
		testAbsenceOfMetadataForQuery("UPDATE testBug9320 SET field1=?");
		testAbsenceOfMetadataForQuery("DELETE FROM testBug9320 WHERE field1=?");
	}

	/**
	 * Tests fix for BUG#9778, DBMD.getTables() shouldn't return tables if views
	 * are asked for, even if the database version doesn't support views.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug9778() throws Exception {
		String tableName = "testBug9778";

		try {
			createTable(tableName, "(field1 int)");
			this.rs = this.conn.getMetaData().getTables(null, null, tableName,
					new String[] { "VIEW" });
			assertEquals(false, this.rs.next());

			this.rs = this.conn.getMetaData().getTables(null, null, tableName,
					new String[] { "TABLE" });
			assertEquals(true, this.rs.next());
		} finally {
			if (this.rs != null) {
				this.rs.close();
				this.rs = null;
			}
		}
	}

	/**
	 * Tests fix for BUG#9769 - Should accept null for procedureNamePattern,
	 * even though it isn't JDBC compliant, for legacy's sake.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug9769() throws Exception {
		boolean defaultPatternConfig = ((com.mysql.jdbc.Connection) this.conn)
				.getNullNamePatternMatchesAll();

		// We're going to change this in 3.2.x, so make that test here, so we
		// catch it.

		if (this.conn.getMetaData().getDriverMajorVersion() == 3
				&& this.conn.getMetaData().getDriverMinorVersion() >= 2) {
			assertEquals(false, defaultPatternConfig);
		} else {
			assertEquals(true, defaultPatternConfig);
		}

		try {
			this.conn.getMetaData().getProcedures(this.conn.getCatalog(), "%",
					null);

			if (!defaultPatternConfig) {
				// we shouldn't have gotten here
				fail("Exception should've been thrown");
			}
		} catch (SQLException sqlEx) {
			if (!defaultPatternConfig) {
				assertEquals(SQLError.SQL_STATE_ILLEGAL_ARGUMENT, sqlEx
						.getSQLState());
			} else {
				throw sqlEx; // we shouldn't have gotten an exception here
			}
		}

		// FIXME: TO test for 3.1.9
		// getColumns();
		// getTablePrivileges();
		// getTables();

	}

	/**
	 * Tests fix for BUG#9917 - Should accept null for catalog in DBMD methods,
	 * even though it's not JDBC-compliant for legacy's sake.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug9917() throws Exception {
		String tableName = "testBug9917";
		boolean defaultCatalogConfig = ((com.mysql.jdbc.Connection) this.conn)
				.getNullCatalogMeansCurrent();

		// We're going to change this in 3.2.x, so make that test here, so we
		// catch it.

		if (this.conn.getMetaData().getDriverMajorVersion() == 3
				&& this.conn.getMetaData().getDriverMinorVersion() >= 2) {
			assertEquals(false, defaultCatalogConfig);
		} else {
			assertEquals(true, defaultCatalogConfig);
		}

		try {
			createTable(tableName, "(field1 int)");
			String currentCatalog = this.conn.getCatalog();

			try {
				this.rs = this.conn.getMetaData().getTables(null, null,
						tableName, new String[] { "TABLE" });

				if (!defaultCatalogConfig) {
					// we shouldn't have gotten here
					fail("Exception should've been thrown");
				}

				assertEquals(true, this.rs.next());
				assertEquals(currentCatalog, this.rs.getString("TABLE_CAT"));

				// FIXME: Methods to test for 3.1.9
				//
				// getBestRowIdentifier()
				// getColumns()
				// getCrossReference()
				// getExportedKeys()
				// getImportedKeys()
				// getIndexInfo()
				// getPrimaryKeys()
				// getProcedures()

			} catch (SQLException sqlEx) {
				if (!defaultCatalogConfig) {
					assertEquals(SQLError.SQL_STATE_ILLEGAL_ARGUMENT, sqlEx
							.getSQLState());
				} else {
					throw sqlEx; // we shouldn't have gotten an exception
					// here
				}
			}

		} finally {
			if (this.rs != null) {
				this.rs.close();
				this.rs = null;
			}
		}
	}

	/**
	 * Tests fix for BUG#11575 -- DBMD.storesLower/Mixed/UpperIdentifiers()
	 * reports incorrect values for servers deployed on Windows.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug11575() throws Exception {
		DatabaseMetaData dbmd = this.conn.getMetaData();

		if (isServerRunningOnWindows()) {
			assertEquals(true, dbmd.storesLowerCaseIdentifiers());
			assertEquals(true, dbmd.storesLowerCaseQuotedIdentifiers());
			assertEquals(false, dbmd.storesMixedCaseIdentifiers());
			assertEquals(false, dbmd.storesMixedCaseQuotedIdentifiers());
			assertEquals(false, dbmd.storesUpperCaseIdentifiers());
			assertEquals(true, dbmd.storesUpperCaseQuotedIdentifiers());
		} else {
			assertEquals(false, dbmd.storesLowerCaseIdentifiers());
			assertEquals(false, dbmd.storesLowerCaseQuotedIdentifiers());
			assertEquals(true, dbmd.storesMixedCaseIdentifiers());
			assertEquals(true, dbmd.storesMixedCaseQuotedIdentifiers());
			assertEquals(false, dbmd.storesUpperCaseIdentifiers());
			assertEquals(true, dbmd.storesUpperCaseQuotedIdentifiers());
		}
	}

	/**
	 * Tests fix for BUG#11781, foreign key information that is quoted is parsed
	 * incorrectly.
	 */
	public void testBug11781() throws Exception {

		if (versionMeetsMinimum(5, 1)) {
			if (!versionMeetsMinimum(5, 2)) {
				// server bug prevents this test from functioning
				
				return;
			}
		}
		
		createTable(
				"`app tab`",
				"( C1 int(11) NULL, INDEX NEWINX (C1), INDEX NEWINX2 (C1)) ENGINE = InnoDB CHECKSUM = 0 COMMENT = 'InnoDB free: 3072 kB; (`C1`) REFER`test/app tab`(`C1`)' PACK_KEYS = 0");

		this.stmt
				.executeUpdate("ALTER TABLE `app tab` ADD CONSTRAINT APPFK FOREIGN KEY (C1) REFERENCES `app tab` (C1)");

		/*
		 * this.rs = this.conn.getMetaData().getCrossReference(
		 * this.conn.getCatalog(), null, "app tab", this.conn.getCatalog(),
		 * null, "app tab");
		 */
		this.rs = ((com.mysql.jdbc.DatabaseMetaData) this.conn.getMetaData())
				.extractForeignKeyFromCreateTable(this.conn.getCatalog(),
						"app tab");
		assertTrue("must return a row", this.rs.next());

		String catalog = this.conn.getCatalog();

		assertEquals("comment; APPFK(`C1`) REFER `" + catalog
				+ "`/ `app tab` (`C1`)", this.rs.getString(3));

		this.rs.close();

		this.rs = this.conn.getMetaData().getImportedKeys(
				this.conn.getCatalog(), null, "app tab");

		assertTrue(this.rs.next());

		this.rs = this.conn.getMetaData().getExportedKeys(
				this.conn.getCatalog(), null, "app tab");

		assertTrue(this.rs.next());
	}

	/**
	 * Tests fix for BUG#12970 - java.sql.Types.OTHER returned for binary and
	 * varbinary columns.
	 * 
	 */
	public void testBug12970() throws Exception {
		if (versionMeetsMinimum(5, 0, 8)) {
			String tableName = "testBug12970";

			createTable(tableName,
					"(binary_field BINARY(32), varbinary_field VARBINARY(64))");

			try {
				this.rs = this.conn.getMetaData().getColumns(
						this.conn.getCatalog(), null, tableName, "%");
				assertTrue(this.rs.next());
				assertEquals(Types.BINARY, this.rs.getInt("DATA_TYPE"));
				assertEquals(32, this.rs.getInt("COLUMN_SIZE"));
				assertTrue(this.rs.next());
				assertEquals(Types.VARBINARY, this.rs.getInt("DATA_TYPE"));
				assertEquals(64, this.rs.getInt("COLUMN_SIZE"));
				this.rs.close();

				this.rs = this.stmt
						.executeQuery("SELECT binary_field, varbinary_field FROM "
								+ tableName);
				ResultSetMetaData rsmd = this.rs.getMetaData();
				assertEquals(Types.BINARY, rsmd.getColumnType(1));
				assertEquals(32, rsmd.getPrecision(1));
				assertEquals(Types.VARBINARY, rsmd.getColumnType(2));
				assertEquals(64, rsmd.getPrecision(2));
				this.rs.close();
			} finally {
				if (this.rs != null) {
					this.rs.close();
				}
			}
		}
	}

	/**
	 * Tests fix for BUG#12975 - OpenOffice expects DBMD.supportsIEF() to return
	 * "true" if foreign keys are supported by the datasource, even though this
	 * method also covers support for check constraints, which MySQL _doesn't_
	 * have.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug12975() throws Exception {
		assertEquals(false, this.conn.getMetaData()
				.supportsIntegrityEnhancementFacility());

		Connection overrideConn = null;

		try {
			Properties props = new Properties();

			props.setProperty("overrideSupportsIntegrityEnhancementFacility",
					"true");

			overrideConn = getConnectionWithProps(props);
			assertEquals(true, overrideConn.getMetaData()
					.supportsIntegrityEnhancementFacility());
		} finally {
			if (overrideConn != null) {
				overrideConn.close();
			}
		}
	}

	/**
	 * Tests fix for BUG#13277 - RSMD for generated keys has NPEs when a
	 * connection is referenced.
	 * 
	 * @throws Exception
	 */
	public void testBug13277() throws Exception {
		if (isRunningOnJdk131()) {
			return; // test not valid on JDK-1.3.1
		}

		createTable("testBug13277",
				"(field1 INT NOT NULL PRIMARY KEY AUTO_INCREMENT, field2 VARCHAR(32))");

		try {
			this.stmt.executeUpdate(
					"INSERT INTO testBug13277 (field2) VALUES ('abcdefg')",
					Statement.RETURN_GENERATED_KEYS);

			this.rs = this.stmt.getGeneratedKeys();

			ResultSetMetaData rsmd = this.rs.getMetaData();
			checkRsmdForBug13277(rsmd);
			this.rs.close();

			for (int i = 0; i < 5; i++) {
				this.stmt
						.addBatch("INSERT INTO testBug13277 (field2) VALUES ('abcdefg')");
			}

			this.stmt.executeBatch();

			this.rs = this.stmt.getGeneratedKeys();

			rsmd = this.rs.getMetaData();
			checkRsmdForBug13277(rsmd);
			this.rs.close();

			this.pstmt = this.conn.prepareStatement(
					"INSERT INTO testBug13277 (field2) VALUES ('abcdefg')",
					Statement.RETURN_GENERATED_KEYS);
			this.pstmt.executeUpdate();

			this.rs = this.pstmt.getGeneratedKeys();

			rsmd = this.rs.getMetaData();
			checkRsmdForBug13277(rsmd);
			this.rs.close();

			this.pstmt.addBatch();
			this.pstmt.addBatch();

			this.pstmt.executeUpdate();

			this.rs = this.pstmt.getGeneratedKeys();

			rsmd = this.rs.getMetaData();
			checkRsmdForBug13277(rsmd);
			this.rs.close();

		} finally {
			if (this.pstmt != null) {
				this.pstmt.close();
				this.pstmt = null;
			}

			if (this.rs != null) {
				this.rs.close();
				this.rs = null;
			}
		}
	}

	/**
	 * Tests BUG13601 (which doesn't seem to be present in 3.1.11, but we'll
	 * leave it in here for regression's-sake).
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug13601() throws Exception {

		if (versionMeetsMinimum(5, 0)) {
			createTable("testBug13601",
					"(field1 BIGINT NOT NULL, field2 BIT default 0 NOT NULL) ENGINE=MyISAM");

			this.rs = this.stmt
					.executeQuery("SELECT field1, field2 FROM testBug13601 WHERE 1=-1");
			ResultSetMetaData rsmd = this.rs.getMetaData();
			assertEquals(Types.BIT, rsmd.getColumnType(2));
			assertEquals(Boolean.class.getName(), rsmd.getColumnClassName(2));

			this.rs = this.conn.prepareStatement(
					"SELECT field1, field2 FROM testBug13601 WHERE 1=-1")
					.executeQuery();
			rsmd = this.rs.getMetaData();
			assertEquals(Types.BIT, rsmd.getColumnType(2));
			assertEquals(Boolean.class.getName(), rsmd.getColumnClassName(2));

		}
	}

	/**
	 * Tests fix for BUG#14815 - DBMD.getColumns() doesn't return TABLE_NAME
	 * correctly.
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testBug14815() throws Exception {
		try {
			createTable("testBug14815_1", "(field_1_1 int)");
			createTable("testBug14815_2", "(field_2_1 int)");

			boolean lcTableNames = this.conn.getMetaData()
					.storesLowerCaseIdentifiers();

			String tableName1 = lcTableNames ? "testbug14815_1"
					: "testBug14815_1";
			String tableName2 = lcTableNames ? "testbug14815_2"
					: "testBug14815_2";

			this.rs = this.conn.getMetaData().getColumns(
					this.conn.getCatalog(), null, "testBug14815%", "%");

			assertTrue(this.rs.next());
			assertEquals(tableName1, this.rs.getString("TABLE_NAME"));
			assertEquals("field_1_1", this.rs.getString("COLUMN_NAME"));

			assertTrue(this.rs.next());
			assertEquals(tableName2, this.rs.getString("TABLE_NAME"));
			assertEquals("field_2_1", this.rs.getString("COLUMN_NAME"));

		} finally {
			if (this.rs != null) {
				this.rs.close();
				this.rs = null;
			}
		}
	}

	/**
	 * Tests fix for BUG#15854 - DBMD.getColumns() returns wrong type for BIT.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug15854() throws Exception {
		if (versionMeetsMinimum(5, 0)) {
			createTable("testBug15854", "(field1 BIT)");
			try {
				this.rs = this.conn.getMetaData().getColumns(
						this.conn.getCatalog(), null, "testBug15854", "field1");
				assertTrue(this.rs.next());
				assertEquals(Types.BIT, this.rs.getInt("DATA_TYPE"));
			} finally {
				if (this.rs != null) {
					ResultSet toClose = this.rs;
					this.rs = null;
					toClose.close();
				}
			}

		}
	}

	/**
	 * Tests fix for BUG#16277 - Invalid classname returned for
	 * RSMD.getColumnClassName() for BIGINT type.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug16277() throws Exception {
		createTable("testBug16277", "(field1 BIGINT, field2 BIGINT UNSIGNED)");
		ResultSetMetaData rsmd = this.stmt.executeQuery(
				"SELECT field1, field2 FROM testBug16277").getMetaData();
		assertEquals("java.lang.Long", rsmd.getColumnClassName(1));
		assertEquals("java.math.BigInteger", rsmd.getColumnClassName(2));
	}

	/**
	 * Tests fix for BUG#18554 - Aliased column names where length of name > 251
	 * are corrupted.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug18554() throws Exception {
		testBug18554(249);
		testBug18554(250);
		testBug18554(251);
		testBug18554(252);
		testBug18554(253);
		testBug18554(254);
		testBug18554(255);
	}

	private void testBug18554(int columnNameLength) throws Exception {
		StringBuffer buf = new StringBuffer(columnNameLength + 2);

		for (int i = 0; i < columnNameLength; i++) {
			buf.append((char) ((Math.random() * 26) + 65));
		}

		String colName = buf.toString();
		this.rs = this.stmt.executeQuery("select curtime() as `" + colName
				+ "`");
		ResultSetMetaData meta = this.rs.getMetaData();

		assertEquals(colName, meta.getColumnLabel(1));

	}

	private void checkRsmdForBug13277(ResultSetMetaData rsmd)
			throws SQLException {
		assertEquals(17, rsmd.getColumnDisplaySize(1));

		if (versionMeetsMinimum(4, 1)) {
			assertEquals(false, rsmd.isDefinitelyWritable(1));
			assertEquals(true, rsmd.isReadOnly(1));
			assertEquals(false, rsmd.isWritable(1));
		}
	}

	public void testSupportsCorrelatedSubqueries() throws Exception {
		DatabaseMetaData dbmd = this.conn.getMetaData();

		assertEquals(versionMeetsMinimum(4, 1), dbmd
				.supportsCorrelatedSubqueries());
	}

	public void testSupportesGroupByUnrelated() throws Exception {
		DatabaseMetaData dbmd = this.conn.getMetaData();

		assertEquals(true, dbmd.supportsGroupByUnrelated());
	}
	
	/**
	 * Tests fix for BUG#21267, ParameterMetaData throws NullPointerException
	 * when prepared SQL actually has a syntax error
	 * 
	 * @throws Exception
	 */
	public void testBug21267() throws Exception {
		if (isRunningOnJdk131()) {
			return; // no parameter metadata on JDK-1.3.1
		}
		
		createTable(
				"bug21267",
				"(`Col1` int(11) NOT NULL,`Col2` varchar(45) default NULL,`Col3` varchar(45) default NULL,PRIMARY KEY  (`Col1`))");

		try {
			this.pstmt = this.conn
					.prepareStatement("SELECT Col1, Col2,Col4 FROM bug21267 WHERE Col1=?");
			this.pstmt.setInt(1, 1);

			java.sql.ParameterMetaData psMeta = this.pstmt
					.getParameterMetaData();

			try {
				assertEquals(0, psMeta.getParameterType(1));
			} catch (SQLException sqlEx) {
				assertEquals(SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, sqlEx.getSQLState());
			}
			
			this.pstmt.close();
			
			Properties props = new Properties();
			props.setProperty("generateSimpleParameterMetadata", "true");
			
			this.pstmt = getConnectionWithProps(props).prepareStatement("SELECT Col1, Col2,Col4 FROM bug21267 WHERE Col1=?");
			
			psMeta = this.pstmt.getParameterMetaData();
			
			assertEquals(Types.VARCHAR, psMeta.getParameterType(1));
		} finally {
			closeMemberJDBCResources();
		}
	}

	/**
	 * Tests fix for BUG#21544 - When using information_schema for metadata, 
	 * COLUMN_SIZE for getColumns() is not clamped to range of 
	 * java.lang.Integer as is the case when not using 
	 * information_schema, thus leading to a truncation exception that 
	 * isn't present when not using information_schema.
	 * 
	 * @throws Exception if the test fails
	 */
	public void testBug21544() throws Exception {
		if (!versionMeetsMinimum(5, 0)) {
			return;
		}
		
		createTable("testBug21544",
                "(foo_id INT NOT NULL, stuff LONGTEXT"
                + ", PRIMARY KEY (foo_id)) TYPE=INNODB");
		
		Connection infoSchemConn = null;
		
		Properties props = new Properties();
		props.setProperty("useInformationSchema", "true");
		props.setProperty("jdbcCompliantTruncation", "false");
		
		infoSchemConn = getConnectionWithProps(props);
		
		try {
	        this.rs = infoSchemConn.getMetaData().getColumns(null, null, 
	        		"testBug21544",
	                null);
	        
	        while (rs.next()) {
	        	rs.getInt("COLUMN_SIZE");   
	        }
	    } finally {
            if (infoSchemConn != null) {
            	infoSchemConn.close();
            }
            
            closeMemberJDBCResources();
        }
	}

	/** 
	 * Tests fix for BUG#22613 - DBMD.getColumns() does not return expected
	 * COLUMN_SIZE for the SET type (fixed to be consistent with the ODBC driver)
	 * 
	 * @throws Exception if the test fails
	 */
	public void testBug22613() throws Exception {
		
		createTable("bug22613", "( s set('a','bc','def','ghij') default NULL, t enum('a', 'ab', 'cdef'))");

		try {
			checkMetadataForBug22613(this.conn);
			
			if (versionMeetsMinimum(5, 0)) {
				Connection infoSchemConn = null;
			
				try {
					Properties props = new Properties();
					props.setProperty("useInformationSchema", "true");
					
					infoSchemConn = getConnectionWithProps(props);
					
					checkMetadataForBug22613(infoSchemConn);
				} finally {
					if (infoSchemConn != null) {
						infoSchemConn.close();
					}
				}
			}
		} finally {
			closeMemberJDBCResources();
		}
	}
	
	private void checkMetadataForBug22613(Connection c) throws Exception {
		String maxValue = "a,bc,def,ghij";
		
		try {
			DatabaseMetaData meta = c.getMetaData();
			this.rs = meta.getColumns(null, this.conn.getCatalog(), "bug22613", "s");
			this.rs.first();

			assertEquals(maxValue.length(), rs.getInt("COLUMN_SIZE"));
			
			this.rs = meta.getColumns(null, c.getCatalog(), "bug22613", "t");
			this.rs.first();

			assertEquals(4, rs.getInt("COLUMN_SIZE"));			
		} finally {
			closeMemberJDBCResources();
		}
	}

	/**
	 * Fix for BUG#22628 - Driver.getPropertyInfo() throws NullPointerException for URL that only specifies
	 * host and/or port.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug22628() throws Exception {
		DriverPropertyInfo[] dpi = new NonRegisteringDriver().getPropertyInfo("jdbc:mysql://bogus:9999", 
				new Properties());
		
		boolean foundHost = false;
		
		for (int i = 0; i < dpi.length; i++) {
			if ("bogus:9999".equals(dpi[i].value)) {
				foundHost = true;
				break;
			}
		}
		
		assertTrue(foundHost);
	}

	private void testAbsenceOfMetadataForQuery(String query) throws Exception {
		try {
			this.pstmt = this.conn.prepareStatement(query);
			ResultSetMetaData rsmd = this.pstmt.getMetaData();

			assertNull(rsmd);

			this.pstmt = ((com.mysql.jdbc.Connection) this.conn)
					.clientPrepareStatement(query);
			rsmd = this.pstmt.getMetaData();

			assertNull(rsmd);
		} finally {
			if (this.pstmt != null) {
				this.pstmt.close();
			}
		}
	}

	public void testRSMDToStringFromDBMD() throws Exception {
		try {		
			this.rs = this.conn.getMetaData().getTypeInfo();
			
			this.rs.getMetaData().toString(); // used to cause NPE
		} finally {
			closeMemberJDBCResources();
		}
	}
	
	public void testCharacterSetForDBMD() throws Exception {
		if (versionMeetsMinimum(4, 0)) {
			// server is broken, fixed in 5.2/6.0?
			
			if (!versionMeetsMinimum(5, 2)) {
				return;
			}
		}
		
		String quoteChar = this.conn.getMetaData().getIdentifierQuoteString();
		
		String tableName = quoteChar + "\u00e9\u0074\u00e9" + quoteChar;
		createTable(tableName, "(field1 int)");
		this.rs = this.conn.getMetaData().getTables(this.conn.getCatalog(), 
				null, tableName, new String[] {"TABLE"});
		assertEquals(true, this.rs.next());
		System.out.println(this.rs.getString("TABLE_NAME"));
		System.out.println(new String(this.rs.getBytes("TABLE_NAME"), "UTF-8"));
	}

	/**
	 * Tests fix for BUG#18258 - Nonexistent catalog/database causes SQLException
	 * to be raised, rather than returning empty result set.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug18258() throws Exception {
		String bogusDatabaseName = "abcdefghijklmnopqrstuvwxyz";
		this.conn.getMetaData().getTables(bogusDatabaseName, "%", "%", new String[] {"TABLE", "VIEW"});
		this.conn.getMetaData().getColumns(bogusDatabaseName, "%", "%", "%");
		this.conn.getMetaData().getProcedures(bogusDatabaseName, "%", "%");
	}

	/**
	 * Tests fix for BUG#23303 - DBMD.getSchemas() doesn't return a TABLE_CATALOG column.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug23303() throws Exception {
		try {
			this.rs = this.conn.getMetaData().getSchemas();
			this.rs.findColumn("TABLE_CATALOG");
		} finally {
			closeMemberJDBCResources();
		}
	}
	
	/**
	 * Tests fix for BUG#23304 - DBMD using "show" and DBMD using 
	 * information_schema do not return results consistent with eachother.
	 * 
	 * (note this fix only addresses the inconsistencies, not the issue that
	 * the driver is treating schemas differently than some users expect.
	 * 
	 * We will revisit this behavior when there is full support for schemas
	 * in MySQL).
	 * 
	 * @throws Exception
	 */
	
	public void testBug23304() throws Exception {
		if (!versionMeetsMinimum(5, 0)) {
			return;
		}
		
		Connection connShow = null;
		Connection connInfoSchema = null;
		
		ResultSet rsShow = null;
		ResultSet rsInfoSchema = null;
		
		try {
			Properties noInfoSchemaProps = new Properties();
			noInfoSchemaProps.setProperty("useInformationSchema", "false");
			
			Properties infoSchemaProps = new Properties();
			infoSchemaProps.setProperty("useInformationSchema", "true");
			infoSchemaProps.setProperty("dumpQueriesOnException", "true");
			
			connShow = getConnectionWithProps(noInfoSchemaProps);
			connInfoSchema = getConnectionWithProps(infoSchemaProps);
			
			DatabaseMetaData dbmdUsingShow = connShow.getMetaData();
			DatabaseMetaData dbmdUsingInfoSchema = connInfoSchema.getMetaData();
			
			assertNotSame(dbmdUsingShow.getClass(), dbmdUsingInfoSchema.getClass());
			
			if (!isRunningOnJdk131()) {
				rsShow = dbmdUsingShow.getSchemas();
				rsInfoSchema = dbmdUsingInfoSchema.getSchemas();
			
				compareResultSets(rsShow, rsInfoSchema);	
			}
			
			/*
			rsShow = dbmdUsingShow.getTables(connShow.getCatalog(), null, "%", new String[] {"TABLE", "VIEW"});
			rsInfoSchema = dbmdUsingInfoSchema.getTables(connInfoSchema.getCatalog(), null, "%", new String[] {"TABLE", "VIEW"});
			
			compareResultSets(rsShow, rsInfoSchema);
			
			rsShow = dbmdUsingShow.getTables(null, null, "%", new String[] {"TABLE", "VIEW"});
			rsInfoSchema = dbmdUsingInfoSchema.getTables(null, null, "%", new String[] {"TABLE", "VIEW"});
		
			compareResultSets(rsShow, rsInfoSchema);
			*/
			
			createTable("t_testBug23304", "(field1 int primary key not null, field2 tinyint, field3 mediumint, field4 mediumint, field5 bigint, field6 float, field7 double, field8 decimal, field9 char(32), field10 varchar(32), field11 blob, field12 mediumblob, field13 longblob, field14 text, field15 mediumtext, field16 longtext, field17 date, field18 time, field19 datetime, field20 timestamp)");
			
			rsShow = dbmdUsingShow.getColumns(connShow.getCatalog(), null, "t_testBug23304", "%");
			rsInfoSchema = dbmdUsingInfoSchema.getColumns(connInfoSchema.getCatalog(), null, "t_testBug23304", "%");
			
			compareResultSets(rsShow, rsInfoSchema);
		} finally {
			if (rsShow != null) {
				rsShow.close();
			}
			
			if (rsInfoSchema != null) {
				rsInfoSchema.close();
			}
		}
	}
	
	private void compareResultSets(ResultSet expected, ResultSet actual) throws Exception {
		if (expected == null && actual != null) {
			fail("Expected null result set, actual was not null.");
		} else if (expected != null && actual == null) {
			fail("Expected non-null actual result set.");
		} else if (expected == null && actual == null) {
			return;
		}
		
		expected.last();
		
		int expectedRows = expected.getRow();
		
		actual.last();
		
		int actualRows = actual.getRow();
		
		assertEquals(expectedRows, actualRows);
		
		ResultSetMetaData metadataExpected = expected.getMetaData();
		ResultSetMetaData metadataActual = actual.getMetaData();
		
		assertEquals(metadataExpected.getColumnCount(), metadataActual.getColumnCount());
		
		for (int i = 0; i < metadataExpected.getColumnCount(); i++) {
			assertEquals(metadataExpected.getColumnName(i + 1), metadataActual.getColumnName(i + 1));
			assertEquals(metadataExpected.getColumnType(i + 1), metadataActual.getColumnType(i + 1));
			assertEquals(metadataExpected.getColumnClassName(i + 1), metadataActual.getColumnClassName(i + 1));
		}
		
		expected.beforeFirst();
		actual.beforeFirst();
		
		StringBuffer messageBuf = null;
		
		while (expected.next() && actual.next()) {
			
			if (messageBuf != null) {
				messageBuf.append("\n");
			}
			
			for (int i = 0; i < metadataExpected.getColumnCount(); i++) {
				if (expected.getObject(i + 1) == null && actual.getObject(i + 1) == null) {
					continue;
				}
				
				if ((expected.getObject(i + 1) == null && actual.getObject(i + 1) != null) ||
						(expected.getObject(i + 1) != null && actual.getObject(i + 1) == null) ||
						(!expected.getObject(i + 1).equals(actual.getObject(i + 1)))) {
					if ("COLUMN_DEF".equals(metadataExpected.getColumnName(i + 1)) && 
							(expected.getObject(i + 1) == null && actual.getString(i + 1).length() == 0) ||
							(expected.getString(i + 1).length() == 0 && actual.getObject(i + 1) == null)) {
						continue; // known bug with SHOW FULL COLUMNS, and we can't distinguish between null and ''
						          // for a default
					}
					
					if (messageBuf == null) {
						messageBuf = new StringBuffer();
					} else {
						messageBuf.append("\n");
					}
					
					messageBuf.append("On row " + expected.getRow() + " ,for column named " + metadataExpected.getColumnName(i + 1) + ", expected '" + expected.getObject(i + 1) + "', found '" + actual.getObject(i + 1) + "'");
					
				}
			}
		}
		
		if (messageBuf != null) {
			fail(messageBuf.toString());
		}
	}
	
	/**
	 * Tests fix for BUG#25624 - Whitespace surrounding storage/size specifiers in stored procedure
	 * declaration causes NumberFormatException to be thrown when calling stored procedure.
	 * 
	 * @throws Exception
	 */
	public void testBug25624() throws Exception {
		if (!versionMeetsMinimum(5, 0)) {
			return;
		}

		//
		// we changed up the parameters to get coverage of the fixes,
		// also note that whitespace _is_ significant in the DDL...
		//
		
		createProcedure(
				"testBug25624",
				"(in _par1 decimal( 10 , 2 ) , in _par2 varchar( 4 )) BEGIN select 1; END");

		this.conn.prepareCall("{call testBug25624(?,?)}").close();
	}
	
	/**
	 * Tests fix for BUG#27867 - Schema objects with identifiers other than
	 * the connection character aren't retrieved correctly in ResultSetMetadata.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug27867() throws Exception {
		try {
			String gbkColumnName = "\u00e4\u00b8\u00ad\u00e6\u2013\u2021\u00e6\u00b5\u2039\u00e8\u00af\u2022";
			createTable("ColumnNameEncoding", "(" + "`" + gbkColumnName
					+ "` varchar(1) default NULL,"
					+ "`ASCIIColumn` varchar(1) default NULL"
					+ ")ENGINE=MyISAM DEFAULT CHARSET=utf8");
			
			this.rs = this.stmt
					.executeQuery("SELECT * FROM ColumnNameEncoding");
			java.sql.ResultSetMetaData tblMD = this.rs.getMetaData();

			assertEquals(gbkColumnName, tblMD.getColumnName(1));
			assertEquals("ASCIIColumn", tblMD.getColumnName(2));
		} finally {
			closeMemberJDBCResources();
		}
	}
	
	/**
	 * Fixed BUG#27915 - DatabaseMetaData.getColumns() doesn't
	 * contain SCOPE_* or IS_AUTOINCREMENT columns.
	 * 
	 * @throws Exception
	 */
	public void testBug27915() throws Exception {
		createTable("testBug27915",
				"(field1 int not null primary key auto_increment, field2 int)");
		DatabaseMetaData dbmd = this.conn.getMetaData();

		try {
			this.rs = dbmd.getColumns(this.conn.getCatalog(), null,
					"testBug27915", "%");
			this.rs.next();

			checkBug27915();

			if (versionMeetsMinimum(5, 0)) {
				this.rs = getConnectionWithProps("useInformationSchema=true")
						.getMetaData().getColumns(this.conn.getCatalog(), null,
								"testBug27915", "%");
				this.rs.next();

				checkBug27915();
			}
		} finally {
			closeMemberJDBCResources();
		}
	}

	private void checkBug27915() throws SQLException {
		assertNull(this.rs.getString("SCOPE_CATALOG"));
		assertNull(this.rs.getString("SCOPE_SCHEMA"));
		assertNull(this.rs.getString("SCOPE_TABLE"));
		assertNull(this.rs.getString("SOURCE_DATA_TYPE"));
		assertEquals("YES", this.rs.getString("IS_AUTOINCREMENT"));

		this.rs.next();
		
		assertNull(this.rs.getString("SCOPE_CATALOG"));
		assertNull(this.rs.getString("SCOPE_SCHEMA"));
		assertNull(this.rs.getString("SCOPE_TABLE"));
		assertNull(this.rs.getString("SOURCE_DATA_TYPE"));
		assertEquals("NO", this.rs.getString("IS_AUTOINCREMENT"));
	}
	
	/**
	 * Tests fix for BUG#27916 - UNSIGNED types not reported
	 * via DBMD.getTypeInfo(), and capitalization of types is
	 * not consistent between DBMD.getColumns(), RSMD.getColumnTypeName()
	 * and DBMD.getTypeInfo().
	 * 
	 * This fix also ensures that the precision of UNSIGNED MEDIUMINT
	 * and UNSIGNED BIGINT is reported correctly via DBMD.getColumns().
	 * 
	 * @throws Exception
	 */
	public void testBug27916() throws Exception {
		createTable(
				"testBug27916",
				"(field1 TINYINT UNSIGNED, field2 SMALLINT UNSIGNED, field3 INT UNSIGNED, field4 INTEGER UNSIGNED, field5 MEDIUMINT UNSIGNED, field6 BIGINT UNSIGNED)");

		ResultSetMetaData rsmd = this.stmt.executeQuery(
				"SELECT * FROM testBug27916").getMetaData();

		HashMap typeNameToPrecision = new HashMap();
		this.rs = this.conn.getMetaData().getTypeInfo();

		while (this.rs.next()) {
			typeNameToPrecision.put(this.rs.getString("TYPE_NAME"), this.rs
					.getObject("PRECISION"));
		}

		this.rs = this.conn.getMetaData().getColumns(this.conn.getCatalog(),
				null, "testBug27916", "%");

		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			this.rs.next();
			String typeName = this.rs.getString("TYPE_NAME");

			assertEquals(typeName, rsmd.getColumnTypeName(i + 1));
			assertEquals(typeName, this.rs.getInt("COLUMN_SIZE"), rsmd
					.getPrecision(i + 1));
			assertEquals(typeName, new Integer(rsmd.getPrecision(i + 1)),
					typeNameToPrecision.get(typeName));
		}
	}
}
