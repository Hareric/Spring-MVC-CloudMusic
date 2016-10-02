/*
 Copyright (C) 2002-2004 MySQL AB

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
package testsuite.simple;

import testsuite.BaseTestCase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import com.mysql.jdbc.SQLError;

/**
 * 
 * @author Mark Matthews
 * @version $Id: DateTest.java 5345 2006-06-01 20:18:04Z mmatthews $
 */
public class DateTest extends BaseTestCase {
	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Creates a new DateTest object.
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 */
	public DateTest(String name) {
		super(name);
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Runs all test cases in this test suite
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(DateTest.class);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void setUp() throws Exception {
		super.setUp();
		createTestTable();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	public void testTimestamp() throws SQLException {
		this.pstmt = this.conn
				.prepareStatement("INSERT INTO DATETEST(tstamp, dt, dtime, tm) VALUES (?, ?, ?, ?)");

		// TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, 6);
		cal.set(Calendar.DAY_OF_MONTH, 3);
		cal.set(Calendar.YEAR, 2002);
		cal.set(Calendar.HOUR, 7);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
		cal.getTime();
		System.out.println(cal);

		// DateFormat df = SimpleDateFormat.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

		Calendar calGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		// df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Timestamp nowTstamp = new Timestamp(cal.getTime().getTime());
		java.sql.Date nowDate = new java.sql.Date(cal.getTime().getTime());
		Timestamp nowDatetime = new Timestamp(cal.getTime().getTime());
		java.sql.Time nowTime = new java.sql.Time(cal.getTime().getTime());
		System.out
				.println("** Times with given calendar (before storing) **\n");
		System.out.println("TIMESTAMP:\t" + nowTstamp.getTime() + " -> "
				+ df.format(nowTstamp));
		System.out.println("DATE:\t\t" + nowDate.getTime() + " -> "
				+ df.format(nowDate));
		System.out.println("DATETIME:\t" + nowDatetime.getTime() + " -> "
				+ df.format(nowDatetime));
		System.out.println("DATE:\t\t" + nowDate.getTime() + " -> "
				+ df.format(nowDate));
		System.out.println("TIME:\t\t" + nowTime.getTime() + " -> "
				+ df.format(nowTime));
		System.out.println("\n");
		this.pstmt.setTimestamp(1, nowTstamp, calGMT);
		// have to use the same TimeZone as used to create or there will be
		// shift
		this.pstmt.setDate(2, nowDate, cal);
		this.pstmt.setTimestamp(3, nowDatetime, calGMT);
		// have to use the same TimeZone as used to create or there will be
		// shift
		this.pstmt.setTime(4, nowTime, cal);
		this.pstmt.execute();

		this.pstmt.getUpdateCount();
		this.pstmt.clearParameters();
		this.rs = this.stmt.executeQuery("SELECT * from DATETEST");

		java.sql.Date thenDate = null;

		while (this.rs.next()) {
			Timestamp thenTstamp = this.rs.getTimestamp(1, calGMT);
			thenDate = this.rs.getDate(2, cal);

			java.sql.Timestamp thenDatetime = this.rs.getTimestamp(3, calGMT);

			java.sql.Time thenTime = this.rs.getTime(4, cal);
			System.out
					.println("** Times with given calendar (retrieved from database) **\n");
			System.out.println("TIMESTAMP:\t" + thenTstamp.getTime() + " -> "
					+ df.format(thenTstamp));
			System.out.println("DATE:\t\t" + thenDate.getTime() + " -> "
					+ df.format(thenDate));
			System.out.println("DATETIME:\t" + thenDatetime.getTime() + " -> "
					+ df.format(thenDatetime));
			System.out.println("TIME:\t\t" + thenTime.getTime() + " -> "
					+ df.format(thenTime));
			System.out.println("\n");
		}

		this.rs.close();
		this.rs = null;
	}

	public void testNanosParsing() throws SQLException {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testNanosParsing");
			this.stmt
					.executeUpdate("CREATE TABLE testNanosParsing (dateIndex int, field1 VARCHAR(32))");
			this.stmt
					.executeUpdate("INSERT INTO testNanosParsing VALUES (1, '1969-12-31 18:00:00.0'), "
							+ "(2, '1969-12-31 18:00:00.90'), "
							+ "(3, '1969-12-31 18:00:00.900'), "
							+ "(4, '1969-12-31 18:00:00.9000'), "
							+ "(5, '1969-12-31 18:00:00.90000'), "
							+ "(6, '1969-12-31 18:00:00.900000'), "
							+ "(7, '1969-12-31 18:00:00.')");

			this.rs = this.stmt
					.executeQuery("SELECT field1 FROM testNanosParsing ORDER BY dateIndex ASC");
			assertTrue(this.rs.next());
			assertTrue(this.rs.getTimestamp(1).getNanos() == 0);
			assertTrue(this.rs.next());
			assertTrue(this.rs.getTimestamp(1).getNanos() + " != 90", this.rs
					.getTimestamp(1).getNanos() == 90);
			assertTrue(this.rs.next());
			assertTrue(this.rs.getTimestamp(1).getNanos() + " != 900", this.rs
					.getTimestamp(1).getNanos() == 900);
			assertTrue(this.rs.next());
			assertTrue(this.rs.getTimestamp(1).getNanos() + " != 9000", this.rs
					.getTimestamp(1).getNanos() == 9000);
			assertTrue(this.rs.next());
			assertTrue(this.rs.getTimestamp(1).getNanos() + " != 90000",
					this.rs.getTimestamp(1).getNanos() == 90000);
			assertTrue(this.rs.next());
			assertTrue(this.rs.getTimestamp(1).getNanos() + " != 900000",
					this.rs.getTimestamp(1).getNanos() == 900000);
			assertTrue(this.rs.next());

			try {
				this.rs.getTimestamp(1);
			} catch (SQLException sqlEx) {
				assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
						.getSQLState()));
			}
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testNanosParsing");
		}
	}

	private void createTestTable() throws SQLException {
		//
		// Catch the error, the table might exist
		//
		try {
			this.stmt.executeUpdate("DROP TABLE DATETEST");
		} catch (SQLException SQLE) {
			;
		}

		this.stmt
				.executeUpdate("CREATE TABLE DATETEST (tstamp TIMESTAMP, dt DATE, dtime DATETIME, tm TIME)");
	}

	/**
	 * Tests the configurability of all-zero date/datetime/timestamp handling in
	 * the driver.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testZeroDateBehavior() throws Exception {
		try {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testZeroDateBehavior");
			this.stmt
					.executeUpdate("CREATE TABLE testZeroDateBehavior(fieldAsString VARCHAR(32), fieldAsDateTime DATETIME)");
			this.stmt
					.executeUpdate("INSERT INTO testZeroDateBehavior VALUES ('0000-00-00 00:00:00', '0000-00-00 00:00:00')");
			Properties props = new Properties();
			props.setProperty("zeroDateTimeBehavior", "round");
			Connection roundConn = getConnectionWithProps(props);
			Statement roundStmt = roundConn.createStatement();
			this.rs = roundStmt
					.executeQuery("SELECT fieldAsString, fieldAsDateTime FROM testZeroDateBehavior");
			this.rs.next();

			assertEquals("0001-01-01", this.rs.getDate(1).toString());
			assertEquals("0001-01-01 00:00:00.0", 
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0", Locale.US).format(this.rs.getTimestamp(1)));
			assertEquals("0001-01-01", this.rs.getDate(2).toString());
			assertEquals("0001-01-01 00:00:00.0", 
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0", Locale.US).format(this.rs.getTimestamp(2)));

			PreparedStatement roundPrepStmt = roundConn
					.prepareStatement("SELECT fieldAsString, fieldAsDateTime FROM testZeroDateBehavior");
			this.rs = roundPrepStmt.executeQuery();
			this.rs.next();

			assertEquals("0001-01-01", this.rs.getDate(1).toString());
			assertEquals("0001-01-01 00:00:00.0", 
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0", Locale.US).format(this.rs.getTimestamp(1)));
			assertEquals("0001-01-01", this.rs.getDate(2).toString());
			assertEquals("0001-01-01 00:00:00.0", 
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0", Locale.US).format(this.rs.getTimestamp(2)));

			props = new Properties();
			props.setProperty("zeroDateTimeBehavior", "convertToNull");
			Connection nullConn = getConnectionWithProps(props);
			Statement nullStmt = nullConn.createStatement();
			this.rs = nullStmt
					.executeQuery("SELECT fieldAsString, fieldAsDateTime FROM testZeroDateBehavior");

			this.rs.next();

			assertTrue(null == this.rs.getDate(1));
			assertTrue(null == this.rs.getTimestamp(1));
			assertTrue(null == this.rs.getDate(2));
			assertTrue(null == this.rs.getTimestamp(2));

			PreparedStatement nullPrepStmt = nullConn
					.prepareStatement("SELECT fieldAsString, fieldAsDateTime FROM testZeroDateBehavior");
			this.rs = nullPrepStmt.executeQuery();

			this.rs.next();

			assertTrue(null == this.rs.getDate(1));
			assertTrue(null == this.rs.getTimestamp(1));
			assertTrue(null == this.rs.getDate(2));
			assertTrue(null == this.rs.getTimestamp(2));
			assertTrue(null == this.rs.getString(2));

			props = new Properties();
			props.setProperty("zeroDateTimeBehavior", "exception");
			Connection exceptionConn = getConnectionWithProps(props);
			Statement exceptionStmt = exceptionConn.createStatement();
			this.rs = exceptionStmt
					.executeQuery("SELECT fieldAsString, fieldAsDateTime FROM testZeroDateBehavior");

			this.rs.next();

			try {
				this.rs.getDate(1);
				fail("Exception should have been thrown when trying to retrieve invalid date");
			} catch (SQLException sqlEx) {
				assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
						.getSQLState()));
			}

			try {
				this.rs.getTimestamp(1);
				fail("Exception should have been thrown when trying to retrieve invalid date");
			} catch (SQLException sqlEx) {
				assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
						.getSQLState()));
			}

			try {
				this.rs.getDate(2);
				fail("Exception should have been thrown when trying to retrieve invalid date");
			} catch (SQLException sqlEx) {
				assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
						.getSQLState()));
			}

			try {
				this.rs.getTimestamp(2);
				fail("Exception should have been thrown when trying to retrieve invalid date");
			} catch (SQLException sqlEx) {
				assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
						.getSQLState()));
			}

			PreparedStatement exceptionPrepStmt = exceptionConn
					.prepareStatement("SELECT fieldAsString, fieldAsDateTime FROM testZeroDateBehavior");

			try {
				this.rs = exceptionPrepStmt.executeQuery();
				this.rs.next();
				this.rs.getDate(2);
				fail("Exception should have been thrown when trying to retrieve invalid date");
			} catch (SQLException sqlEx) {
				assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(sqlEx
						.getSQLState()));
			}

		} finally {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testZeroDateBehavior");
		}
	}

	public void testReggieBug() throws Exception {
		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testReggieBug");
			this.stmt.executeUpdate("CREATE TABLE testReggieBug (field1 DATE)");

			PreparedStatement pStmt = this.conn
					.prepareStatement("INSERT INTO testReggieBug VALUES (?)");
			pStmt.setDate(1, new Date(2004 - 1900, 07, 28));
			pStmt.executeUpdate();
			this.rs = this.stmt.executeQuery("SELECT * FROM testReggieBug");
			this.rs.next();
			System.out.println(this.rs.getDate(1));
			this.rs = this.conn.prepareStatement("SELECT * FROM testReggieBug")
					.executeQuery();
			this.rs.next();
			System.out.println(this.rs.getDate(1));

		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testReggieBug");
		}
	}
	
	public void testNativeConversions() throws Exception {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		Date dt = new Date(ts.getTime());
		Time tm = new Time(ts.getTime());
		
		createTable("testNativeConversions", "(time_field TIME, date_field DATE, datetime_field DATETIME, timestamp_field TIMESTAMP)");
		this.pstmt = this.conn.prepareStatement("INSERT INTO testNativeConversions VALUES (?,?,?,?)");
		this.pstmt.setTime(1, tm);
		this.pstmt.setDate(2, dt);
		this.pstmt.setTimestamp(3, ts);
		this.pstmt.setTimestamp(4, ts);
		this.pstmt.execute();
		this.pstmt.close();
		
		this.pstmt = this.conn.prepareStatement("SELECT time_field, date_field, datetime_field, timestamp_field FROM testNativeConversions");
		this.rs = this.pstmt.executeQuery();
		assertTrue(this.rs.next());
		System.out.println(this.rs.getTime(1));
		System.out.println(this.rs.getTime(2));
		System.out.println(this.rs.getTime(3));
		System.out.println(this.rs.getTime(4));
		System.out.println();
		System.out.println(this.rs.getDate(1));
		System.out.println(this.rs.getDate(2));
		System.out.println(this.rs.getDate(3));
		System.out.println(this.rs.getDate(4));
		System.out.println();
		System.out.println(this.rs.getTimestamp(1));
		System.out.println(this.rs.getTimestamp(2));
		System.out.println(this.rs.getTimestamp(3));
		System.out.println(this.rs.getTimestamp(4));
	}
		
}
