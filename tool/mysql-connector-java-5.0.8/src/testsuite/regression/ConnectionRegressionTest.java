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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import testsuite.BaseTestCase;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.ReplicationConnection;
import com.mysql.jdbc.ReplicationDriver;
import com.mysql.jdbc.log.StandardLogger;

/**
 * Regression tests for Connections
 * 
 * @author Mark Matthews
 * @version $Id: ConnectionRegressionTest.java,v 1.1.2.1 2005/05/13 18:58:38
 *          mmatthews Exp $
 */
public class ConnectionRegressionTest extends BaseTestCase {
	/**
	 * DOCUMENT ME!
	 * 
	 * @param name
	 *            the name of the testcase
	 */
	public ConnectionRegressionTest(String name) {
		super(name);
	}

	/**
	 * Runs all test cases in this test suite
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ConnectionRegressionTest.class);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             ...
	 */
	public void testBug1914() throws Exception {
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), BIGINT)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), BINARY)}"));
		System.out
				.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), BIT)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), CHAR)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), DATE)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), DECIMAL)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), DOUBLE)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), FLOAT)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), INTEGER)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), LONGVARBINARY)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), LONGVARCHAR)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), TIME)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), TIMESTAMP)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), TINYINT)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), VARBINARY)}"));
		System.out.println(this.conn
				.nativeSQL("{fn convert(foo(a,b,c), VARCHAR)}"));
	}

	/**
	 * Tests fix for BUG#3554 - Not specifying database in URL causes
	 * MalformedURL exception.
	 * 
	 * @throws Exception
	 *             if an error ocurrs.
	 */
	public void testBug3554() throws Exception {
		try {
			new NonRegisteringDriver().connect(
					"jdbc:mysql://localhost:3306/?user=root&password=root",
					new Properties());
		} catch (SQLException sqlEx) {
			assertTrue(sqlEx.getMessage().indexOf("Malformed") == -1);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             ...
	 */
	public void testBug3790() throws Exception {
		String field2OldValue = "foo";
		String field2NewValue = "bar";
		int field1OldValue = 1;

		Connection conn1 = null;
		Connection conn2 = null;
		Statement stmt1 = null;
		Statement stmt2 = null;
		ResultSet rs2 = null;

		Properties props = new Properties();

		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3790");
			this.stmt
					.executeUpdate("CREATE TABLE testBug3790 (field1 INT NOT NULL PRIMARY KEY, field2 VARCHAR(32)) TYPE=InnoDB");
			this.stmt.executeUpdate("INSERT INTO testBug3790 VALUES ("
					+ field1OldValue + ", '" + field2OldValue + "')");

			conn1 = getConnectionWithProps(props); // creates a new connection
			conn2 = getConnectionWithProps(props); // creates another new
			// connection
			conn1.setAutoCommit(false);
			conn2.setAutoCommit(false);

			stmt1 = conn1.createStatement();
			stmt1.executeUpdate("UPDATE testBug3790 SET field2 = '"
					+ field2NewValue + "' WHERE field1=" + field1OldValue);
			conn1.commit();

			stmt2 = conn2.createStatement();

			rs2 = stmt2.executeQuery("SELECT field1, field2 FROM testBug3790");

			assertTrue(rs2.next());
			assertTrue(rs2.getInt(1) == field1OldValue);
			assertTrue(rs2.getString(2).equals(field2NewValue));
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3790");

			if (rs2 != null) {
				rs2.close();
			}

			if (stmt2 != null) {
				stmt2.close();
			}

			if (stmt1 != null) {
				stmt1.close();
			}

			if (conn1 != null) {
				conn1.close();
			}

			if (conn2 != null) {
				conn2.close();
			}
		}
	}

	/**
	 * Tests if the driver configures character sets correctly for 4.1.x
	 * servers. Requires that the 'admin connection' is configured, as this test
	 * needs to create/drop databases.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void testCollation41() throws Exception {
		if (versionMeetsMinimum(4, 1) && isAdminConnectionConfigured()) {
			Map charsetsAndCollations = getCharacterSetsAndCollations();
			charsetsAndCollations.remove("latin7"); // Maps to multiple Java
			// charsets
			charsetsAndCollations.remove("ucs2"); // can't be used as a
			// connection charset

			Iterator charsets = charsetsAndCollations.keySet().iterator();

			while (charsets.hasNext()) {
				Connection charsetConn = null;
				Statement charsetStmt = null;

				try {
					String charsetName = charsets.next().toString();
					String collationName = charsetsAndCollations.get(
							charsetName).toString();
					Properties props = new Properties();
					props.put("characterEncoding", charsetName);

					System.out.println("Testing character set " + charsetName);

					charsetConn = getAdminConnectionWithProps(props);

					charsetStmt = charsetConn.createStatement();

					charsetStmt
							.executeUpdate("DROP DATABASE IF EXISTS testCollation41");
					charsetStmt
							.executeUpdate("DROP TABLE IF EXISTS testCollation41");

					charsetStmt
							.executeUpdate("CREATE DATABASE testCollation41 DEFAULT CHARACTER SET "
									+ charsetName);
					charsetConn.setCatalog("testCollation41");

					// We've switched catalogs, so we need to recreate the
					// statement to pick this up...
					charsetStmt = charsetConn.createStatement();

					StringBuffer createTableCommand = new StringBuffer(
							"CREATE TABLE testCollation41"
									+ "(field1 VARCHAR(255), field2 INT)");

					charsetStmt.executeUpdate(createTableCommand.toString());

					charsetStmt
							.executeUpdate("INSERT INTO testCollation41 VALUES ('abc', 0)");

					int updateCount = charsetStmt
							.executeUpdate("UPDATE testCollation41 SET field2=1 WHERE field1='abc'");
					assertTrue(updateCount == 1);
				} finally {
					if (charsetStmt != null) {
						charsetStmt
								.executeUpdate("DROP TABLE IF EXISTS testCollation41");
						charsetStmt
								.executeUpdate("DROP DATABASE IF EXISTS testCollation41");
						charsetStmt.close();
					}

					if (charsetConn != null) {
						charsetConn.close();
					}
				}
			}
		}
	}

	/**
	 * Tests setReadOnly() being reset during failover
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testSetReadOnly() throws Exception {
		Properties props = new Properties();
		props.put("autoReconnect", "true");

		String sepChar = "?";

		if (BaseTestCase.dbUrl.indexOf("?") != -1) {
			sepChar = "&";
		}

		Connection reconnectableConn = DriverManager.getConnection(
				BaseTestCase.dbUrl + sepChar + "autoReconnect=true", props);

		this.rs = reconnectableConn.createStatement().executeQuery(
				"SELECT CONNECTION_ID()");
		this.rs.next();

		String connectionId = this.rs.getString(1);

		reconnectableConn.setReadOnly(true);

		boolean isReadOnly = reconnectableConn.isReadOnly();

		Connection killConn = getConnectionWithProps((Properties)null);

		killConn.createStatement().executeUpdate("KILL " + connectionId);
		Thread.sleep(2000);

		SQLException caughtException = null;

		int numLoops = 8;

		while (caughtException == null && numLoops > 0) {
			numLoops--;

			try {
				reconnectableConn.createStatement().executeQuery("SELECT 1");
			} catch (SQLException sqlEx) {
				caughtException = sqlEx;
			}
		}

		System.out
				.println("Executing statement on reconnectable connection...");

		this.rs = reconnectableConn.createStatement().executeQuery(
				"SELECT CONNECTION_ID()");
		this.rs.next();
		assertTrue("Connection is not a reconnected-connection", !connectionId
				.equals(this.rs.getString(1)));

		try {
			reconnectableConn.createStatement().executeQuery("SELECT 1");
		} catch (SQLException sqlEx) {
			; // ignore
		}

		reconnectableConn.createStatement().executeQuery("SELECT 1");

		assertTrue(reconnectableConn.isReadOnly() == isReadOnly);
	}

	private Map getCharacterSetsAndCollations() throws Exception {
		Map charsetsToLoad = new HashMap();

		try {
			this.rs = this.stmt.executeQuery("SHOW character set");

			while (this.rs.next()) {
				charsetsToLoad.put(this.rs.getString("Charset"), this.rs
						.getString("Default collation"));
			}

			//
			// These don't have mappings in Java...
			//
			charsetsToLoad.remove("swe7");
			charsetsToLoad.remove("hp8");
			charsetsToLoad.remove("dec8");
			charsetsToLoad.remove("koi8u");
			charsetsToLoad.remove("keybcs2");
			charsetsToLoad.remove("geostd8");
			charsetsToLoad.remove("armscii8");
		} finally {
			if (this.rs != null) {
				this.rs.close();
			}
		}

		return charsetsToLoad;
	}

	/**
	 * Tests fix for BUG#4334, port #'s not being picked up for
	 * failover/autoreconnect.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testBug4334() throws Exception {
		if (isAdminConnectionConfigured()) {
			Connection adminConnection = null;

			try {
				adminConnection = getAdminConnection();

				int bogusPortNumber = 65534;

				NonRegisteringDriver driver = new NonRegisteringDriver();

				Properties oldProps = driver.parseURL(BaseTestCase.dbUrl, null);

				String host = driver.host(oldProps);
				int port = driver.port(oldProps);
				String database = oldProps
						.getProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
				String user = oldProps
						.getProperty(NonRegisteringDriver.USER_PROPERTY_KEY);
				String password = oldProps
						.getProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY);

				StringBuffer newUrlToTestPortNum = new StringBuffer(
						"jdbc:mysql://");

				if (host != null) {
					newUrlToTestPortNum.append(host);
				}

				newUrlToTestPortNum.append(":").append(port);
				newUrlToTestPortNum.append(",");

				if (host != null) {
					newUrlToTestPortNum.append(host);
				}

				newUrlToTestPortNum.append(":").append(bogusPortNumber);
				newUrlToTestPortNum.append("/");

				if (database != null) {
					newUrlToTestPortNum.append(database);
				}

				if ((user != null) || (password != null)) {
					newUrlToTestPortNum.append("?");

					if (user != null) {
						newUrlToTestPortNum.append("user=").append(user);

						if (password != null) {
							newUrlToTestPortNum.append("&");
						}
					}

					if (password != null) {
						newUrlToTestPortNum.append("password=")
								.append(password);
					}
				}

				Properties autoReconnectProps = new Properties();
				autoReconnectProps.put("autoReconnect", "true");

				System.out.println(newUrlToTestPortNum);

				//
				// First test that port #'s are being correctly picked up
				//
				// We do this by looking at the error message that is returned
				//
				Connection portNumConn = DriverManager.getConnection(
						newUrlToTestPortNum.toString(), autoReconnectProps);
				Statement portNumStmt = portNumConn.createStatement();
				this.rs = portNumStmt.executeQuery("SELECT connection_id()");
				this.rs.next();

				killConnection(adminConnection, this.rs.getString(1));

				try {
					portNumStmt.executeQuery("SELECT connection_id()");
				} catch (SQLException sqlEx) {
					// we expect this one
				}

				try {
					portNumStmt.executeQuery("SELECT connection_id()");
				} catch (SQLException sqlEx) {
					assertTrue(sqlEx.getMessage().toLowerCase().indexOf(
							"connection refused") != -1);
				}

				//
				// Now make sure failover works
				//
				StringBuffer newUrlToTestFailover = new StringBuffer(
						"jdbc:mysql://");

				if (host != null) {
					newUrlToTestFailover.append(host);
				}

				newUrlToTestFailover.append(":").append(port);
				newUrlToTestFailover.append(",");

				if (host != null) {
					newUrlToTestFailover.append(host);
				}

				newUrlToTestFailover.append(":").append(bogusPortNumber);
				newUrlToTestFailover.append("/");

				if (database != null) {
					newUrlToTestFailover.append(database);
				}

				if ((user != null) || (password != null)) {
					newUrlToTestFailover.append("?");

					if (user != null) {
						newUrlToTestFailover.append("user=").append(user);

						if (password != null) {
							newUrlToTestFailover.append("&");
						}
					}

					if (password != null) {
						newUrlToTestFailover.append("password=").append(
								password);
					}
				}

				Connection failoverConn = DriverManager.getConnection(
						newUrlToTestFailover.toString(), autoReconnectProps);
				Statement failoverStmt = portNumConn.createStatement();
				this.rs = failoverStmt.executeQuery("SELECT connection_id()");
				this.rs.next();

				killConnection(adminConnection, this.rs.getString(1));

				try {
					failoverStmt.executeQuery("SELECT connection_id()");
				} catch (SQLException sqlEx) {
					// we expect this one
				}

				failoverStmt.executeQuery("SELECT connection_id()");
			} finally {
				if (adminConnection != null) {
					adminConnection.close();
				}
			}
		}
	}

	private static void killConnection(Connection adminConn, String threadId)
			throws SQLException {
		adminConn.createStatement().execute("KILL " + threadId);
	}

	/**
	 * Tests fix for BUG#6966, connections starting up failed-over (due to down
	 * master) never retry master.
	 * 
	 * @throws Exception
	 *             if the test fails...Note, test is timing-dependent, but
	 *             should work in most cases.
	 */
	public void testBug6966() throws Exception {
		Properties props = new Driver().parseURL(BaseTestCase.dbUrl, null);
		props.setProperty("autoReconnect", "true");

		// Re-build the connection information
		int firstIndexOfHost = BaseTestCase.dbUrl.indexOf("//") + 2;
		int lastIndexOfHost = BaseTestCase.dbUrl.indexOf("/", firstIndexOfHost);

		String hostPortPair = BaseTestCase.dbUrl.substring(firstIndexOfHost,
				lastIndexOfHost);

		StringTokenizer st = new StringTokenizer(hostPortPair, ":");

		String host = null;
		String port = null;

		if (st.hasMoreTokens()) {
			String possibleHostOrPort = st.nextToken();

			if (Character.isDigit(possibleHostOrPort.charAt(0)) && 
					(possibleHostOrPort.indexOf(".") == -1 /* IPV4 */)  &&
					(possibleHostOrPort.indexOf("::") == -1 /* IPV6 */)) {
				port = possibleHostOrPort;
				host = "localhost";
			} else {
				host = possibleHostOrPort;
			}
		}

		if (st.hasMoreTokens()) {
			port = st.nextToken();
		}

		if (host == null) {
			host = "";
		}

		if (port == null) {
			port = "3306";
		}

		StringBuffer newHostBuf = new StringBuffer();
		newHostBuf.append(host);
		newHostBuf.append(":65532"); // make sure the master fails
		newHostBuf.append(",");
		newHostBuf.append(host);
		if (port != null) {
			newHostBuf.append(":");
			newHostBuf.append(port);
		}

		props.remove("PORT");

		props.setProperty("HOST", newHostBuf.toString());
		props.setProperty("queriesBeforeRetryMaster", "50");
		props.setProperty("maxReconnects", "1");

		Connection failoverConnection = null;

		try {
			failoverConnection = getConnectionWithProps("jdbc:mysql://"
					+ newHostBuf.toString() + "/", props);
			failoverConnection.setAutoCommit(false);

			String originalConnectionId = getSingleIndexedValueWithQuery(
					failoverConnection, 1, "SELECT CONNECTION_ID()").toString();
			
			for (int i = 0; i < 49; i++) {
				failoverConnection.createStatement().executeQuery("SELECT 1");
			}

			((com.mysql.jdbc.Connection)failoverConnection).clearHasTriedMaster();
			
			failoverConnection.setAutoCommit(true);

			String newConnectionId = getSingleIndexedValueWithQuery(
					failoverConnection, 1, "SELECT CONNECTION_ID()").toString();
			
			assertTrue(((com.mysql.jdbc.Connection)failoverConnection).hasTriedMaster());
			
			assertTrue(!newConnectionId.equals(originalConnectionId));

			failoverConnection.createStatement().executeQuery("SELECT 1");
		} finally {
			if (failoverConnection != null) {
				failoverConnection.close();
			}
		}
	}

	/**
	 * Test fix for BUG#7952 -- Infinite recursion when 'falling back' to master
	 * in failover configuration.
	 * 
	 * @throws Exception
	 *             if the tests fails.
	 */
	public void testBug7952() throws Exception {
		Properties props = new Driver().parseURL(BaseTestCase.dbUrl, null);
		props.setProperty("autoReconnect", "true");

		// Re-build the connection information
		int firstIndexOfHost = BaseTestCase.dbUrl.indexOf("//") + 2;
		int lastIndexOfHost = BaseTestCase.dbUrl.indexOf("/", firstIndexOfHost);

		String hostPortPair = BaseTestCase.dbUrl.substring(firstIndexOfHost,
				lastIndexOfHost);

		StringTokenizer st = new StringTokenizer(hostPortPair, ":");

		String host = null;
		String port = null;

		if (st.hasMoreTokens()) {
			String possibleHostOrPort = st.nextToken();

			if (possibleHostOrPort.indexOf(".") == -1
					&& Character.isDigit(possibleHostOrPort.charAt(0))) {
				port = possibleHostOrPort;
				host = "localhost";
			} else {
				host = possibleHostOrPort;
			}
		}

		if (st.hasMoreTokens()) {
			port = st.nextToken();
		}

		if (host == null) {
			host = "";
		}

		if (port == null) {
			port = "3306";
		}

		StringBuffer newHostBuf = new StringBuffer();
		newHostBuf.append(host);
		newHostBuf.append(":");
		newHostBuf.append(port);
		newHostBuf.append(",");
		newHostBuf.append(host);
		if (port != null) {
			newHostBuf.append(":");
			newHostBuf.append(port);
		}

		props.remove("PORT");

		props.setProperty("HOST", newHostBuf.toString());
		props.setProperty("queriesBeforeRetryMaster", "10");
		props.setProperty("maxReconnects", "1");

		Connection failoverConnection = null;
		Connection killerConnection = getConnectionWithProps((Properties)null);

		try {
			failoverConnection = getConnectionWithProps("jdbc:mysql://"
					+ newHostBuf + "/", props);
			((com.mysql.jdbc.Connection) failoverConnection)
					.setPreferSlaveDuringFailover(true);
			failoverConnection.setAutoCommit(false);

			String failoverConnectionId = getSingleIndexedValueWithQuery(
					failoverConnection, 1, "SELECT CONNECTION_ID()").toString();

			System.out.println("Connection id: " + failoverConnectionId);

			killConnection(killerConnection, failoverConnectionId);

			Thread.sleep(3000); // This can take some time....

			try {
				failoverConnection.createStatement().executeQuery("SELECT 1");
			} catch (SQLException sqlEx) {
				assertTrue("08S01".equals(sqlEx.getSQLState()));
			}

			((com.mysql.jdbc.Connection) failoverConnection)
					.setPreferSlaveDuringFailover(false);
			((com.mysql.jdbc.Connection) failoverConnection)
					.setFailedOver(true);

			failoverConnection.setAutoCommit(true);

			String failedConnectionId = getSingleIndexedValueWithQuery(
					failoverConnection, 1, "SELECT CONNECTION_ID()").toString();
			System.out.println("Failed over connection id: "
					+ failedConnectionId);

			((com.mysql.jdbc.Connection) failoverConnection)
					.setPreferSlaveDuringFailover(false);
			((com.mysql.jdbc.Connection) failoverConnection)
					.setFailedOver(true);

			for (int i = 0; i < 30; i++) {
				failoverConnection.setAutoCommit(true);
				System.out.println(getSingleIndexedValueWithQuery(
						failoverConnection, 1, "SELECT CONNECTION_ID()"));
				// failoverConnection.createStatement().executeQuery("SELECT
				// 1");
				failoverConnection.setAutoCommit(true);
			}

			String fallbackConnectionId = getSingleIndexedValueWithQuery(
					failoverConnection, 1, "SELECT CONNECTION_ID()").toString();
			System.out.println("fallback connection id: "
					+ fallbackConnectionId);

			/*
			 * long begin = System.currentTimeMillis();
			 * 
			 * failoverConnection.setAutoCommit(true);
			 * 
			 * long end = System.currentTimeMillis();
			 * 
			 * assertTrue("Probably didn't try failing back to the
			 * master....check test", (end - begin) > 500);
			 * 
			 * failoverConnection.createStatement().executeQuery("SELECT 1");
			 */
		} finally {
			if (failoverConnection != null) {
				failoverConnection.close();
			}
		}
	}

	/**
	 * Tests fix for BUG#7607 - MS932, SHIFT_JIS and Windows_31J not recog. as
	 * aliases for sjis.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug7607() throws Exception {
		if (versionMeetsMinimum(4, 1)) {
			Connection ms932Conn = null, cp943Conn = null, shiftJisConn = null, windows31JConn = null;

			try {
				Properties props = new Properties();
				props.setProperty("characterEncoding", "MS932");

				ms932Conn = getConnectionWithProps(props);

				this.rs = ms932Conn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				String encoding = this.rs.getString(2);
				if (!versionMeetsMinimum(5, 0, 3)
						&& !versionMeetsMinimum(4, 1, 11)) {
					assertEquals("sjis", encoding.toLowerCase(Locale.ENGLISH));
				} else {
					assertEquals("cp932", encoding.toLowerCase(Locale.ENGLISH));
				}

				this.rs = ms932Conn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());

				String charsetToCheck = "ms932";

				if (versionMeetsMinimum(5, 0, 3)
						|| versionMeetsMinimum(4, 1, 11)) {
					charsetToCheck = "windows-31j";
				}

				assertEquals(charsetToCheck,
						((com.mysql.jdbc.ResultSetMetaData) this.rs
								.getMetaData()).getColumnCharacterSet(1)
								.toLowerCase(Locale.ENGLISH));

				try {
					ms932Conn.createStatement().executeUpdate(
							"drop table if exists testBug7607");
					ms932Conn
							.createStatement()
							.executeUpdate(
									"create table testBug7607 (sortCol int, col1 varchar(100) ) character set sjis");
					ms932Conn.createStatement().executeUpdate(
							"insert into testBug7607 values(1, 0x835C)"); // standard
					// sjis
					ms932Conn.createStatement().executeUpdate(
							"insert into testBug7607 values(2, 0x878A)"); // NEC
					// kanji

					this.rs = ms932Conn
							.createStatement()
							.executeQuery(
									"SELECT col1 FROM testBug7607 ORDER BY sortCol ASC");
					assertTrue(this.rs.next());
					String asString = this.rs.getString(1);
					assertTrue("\u30bd".equals(asString));

					// Can't be fixed unless server is fixed,
					// this is fixed in 4.1.7.

					assertTrue(this.rs.next());
					asString = this.rs.getString(1);
					assertEquals("\u3231", asString);
				} finally {
					ms932Conn.createStatement().executeUpdate(
							"drop table if exists testBug7607");
				}

				props = new Properties();
				props.setProperty("characterEncoding", "SHIFT_JIS");

				shiftJisConn = getConnectionWithProps(props);

				this.rs = shiftJisConn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				encoding = this.rs.getString(2);
				assertTrue("sjis".equalsIgnoreCase(encoding));

				this.rs = shiftJisConn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());

				String charSetUC = ((com.mysql.jdbc.ResultSetMetaData) this.rs
						.getMetaData()).getColumnCharacterSet(1).toUpperCase(
						Locale.US);

				if (isRunningOnJdk131()) {
					assertEquals("WINDOWS-31J", charSetUC);
				} else {
//					assertEquals("SHIFT_JIS", charSetUC);
				}

				props = new Properties();
				props.setProperty("characterEncoding", "WINDOWS-31J");

				windows31JConn = getConnectionWithProps(props);

				this.rs = windows31JConn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				encoding = this.rs.getString(2);

				if (!versionMeetsMinimum(5, 0, 3)
						&& !versionMeetsMinimum(4, 1, 11)) {
					assertEquals("sjis", encoding.toLowerCase(Locale.ENGLISH));
				} else {
					assertEquals("cp932", encoding.toLowerCase(Locale.ENGLISH));
				}

				this.rs = windows31JConn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());

				if (!versionMeetsMinimum(4, 1, 11)) {
					assertEquals("sjis".toLowerCase(Locale.ENGLISH),
							((com.mysql.jdbc.ResultSetMetaData) this.rs
									.getMetaData()).getColumnCharacterSet(1)
									.toLowerCase(Locale.ENGLISH));
				} else {
					assertEquals("windows-31j".toLowerCase(Locale.ENGLISH),
							((com.mysql.jdbc.ResultSetMetaData) this.rs
									.getMetaData()).getColumnCharacterSet(1)
									.toLowerCase(Locale.ENGLISH));
				}

				props = new Properties();
				props.setProperty("characterEncoding", "CP943");

				cp943Conn = getConnectionWithProps(props);

				this.rs = cp943Conn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				encoding = this.rs.getString(2);
				assertTrue("sjis".equalsIgnoreCase(encoding));

				this.rs = cp943Conn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());

				charSetUC = ((com.mysql.jdbc.ResultSetMetaData) this.rs
						.getMetaData()).getColumnCharacterSet(1).toUpperCase(
						Locale.US);

				if (isRunningOnJdk131()) {
					assertEquals("WINDOWS-31J", charSetUC);
				} else {
					assertEquals("CP943", charSetUC);
				}

			} finally {
				if (ms932Conn != null) {
					ms932Conn.close();
				}

				if (shiftJisConn != null) {
					shiftJisConn.close();
				}

				if (windows31JConn != null) {
					windows31JConn.close();
				}

				if (cp943Conn != null) {
					cp943Conn.close();
				}
			}
		}
	}

	/**
	 * In some case Connector/J's round-robin function doesn't work.
	 * 
	 * I had 2 mysqld, node1 "localhost:3306" and node2 "localhost:3307".
	 * 
	 * 1. node1 is up, node2 is up
	 * 
	 * 2. java-program connect to node1 by using properties
	 * "autoRecconect=true","roundRobinLoadBalance=true","failOverReadOnly=false".
	 * 
	 * 3. node1 is down, node2 is up
	 * 
	 * 4. java-program execute a query and fail, but Connector/J's round-robin
	 * fashion failover work and if java-program retry a query it can succeed
	 * (connection is change to node2 by Connector/j)
	 * 
	 * 5. node1 is up, node2 is up
	 * 
	 * 6. node1 is up, node2 is down
	 * 
	 * 7. java-program execute a query, but this time Connector/J doesn't work
	 * althought node1 is up and usable.
	 * 
	 * 
	 * @throws Exception
	 */
	public void testBug8643() throws Exception {
		if (runMultiHostTests()) {
			Properties defaultProps = getMasterSlaveProps();

			defaultProps.remove(NonRegisteringDriver.HOST_PROPERTY_KEY);
			defaultProps.remove(NonRegisteringDriver.PORT_PROPERTY_KEY);

			defaultProps.put("autoReconnect", "true");
			defaultProps.put("roundRobinLoadBalance", "true");
			defaultProps.put("failOverReadOnly", "false");

			Connection con = null;
			try {
				con = DriverManager.getConnection(getMasterSlaveUrl(),
						defaultProps);
				Statement stmt1 = con.createStatement();

				ResultSet rs1 = stmt1
						.executeQuery("show variables like 'port'");
				rs1.next();

				rs1 = stmt1.executeQuery("select connection_id()");
				rs1.next();
				String originalConnectionId = rs1.getString(1);
				this.stmt.executeUpdate("kill " + originalConnectionId);

				int numLoops = 8;

				SQLException caughtException = null;

				while (caughtException == null && numLoops > 0) {
					numLoops--;

					try {
						rs1 = stmt1.executeQuery("show variables like 'port'");
					} catch (SQLException sqlEx) {
						caughtException = sqlEx;
					}
				}

				assertNotNull(caughtException);

				// failover and retry
				rs1 = stmt1.executeQuery("show variables like 'port'");

				rs1.next();
				assertTrue(!((com.mysql.jdbc.Connection) con)
						.isMasterConnection());

				rs1 = stmt1.executeQuery("select connection_id()");
				rs1.next();
				String nextConnectionId = rs1.getString(1);
				assertTrue(!nextConnectionId.equals(originalConnectionId));

				this.stmt.executeUpdate("kill " + nextConnectionId);

				numLoops = 8;

				caughtException = null;

				while (caughtException == null && numLoops > 0) {
					numLoops--;

					try {
						rs1 = stmt1.executeQuery("show variables like 'port'");
					} catch (SQLException sqlEx) {
						caughtException = sqlEx;
					}
				}

				assertNotNull(caughtException);

				// failover and retry
				rs1 = stmt1.executeQuery("show variables like 'port'");

				rs1.next();
				assertTrue(((com.mysql.jdbc.Connection) con)
						.isMasterConnection());

			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Tests fix for BUG#9206, can not use 'UTF-8' for characterSetResults
	 * configuration property.
	 */
	public void testBug9206() throws Exception {
		Properties props = new Properties();
		props.setProperty("characterSetResults", "UTF-8");
		getConnectionWithProps(props).close();
	}

	/**
	 * These two charsets have different names depending on version of MySQL
	 * server.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testNewCharsetsConfiguration() throws Exception {
		Properties props = new Properties();
		props.setProperty("useUnicode", "true");
		props.setProperty("characterEncoding", "EUC_KR");
		getConnectionWithProps(props).close();

		props = new Properties();
		props.setProperty("useUnicode", "true");
		props.setProperty("characterEncoding", "KOI8_R");
		getConnectionWithProps(props).close();
	}

	/**
	 * Tests fix for BUG#10144 - Memory leak in ServerPreparedStatement if
	 * serverPrepare() fails.
	 */

	public void testBug10144() throws Exception {
		if (versionMeetsMinimum(4, 1)) {
			Properties props = new Properties();
			props.setProperty("emulateUnsupportedPstmts", "false");
			props.setProperty("useServerPrepStmts", "true");

			Connection bareConn = getConnectionWithProps(props);

			int currentOpenStatements = ((com.mysql.jdbc.Connection) bareConn)
					.getActiveStatementCount();

			try {
				bareConn.prepareStatement("Boo!");
				fail("Should not've been able to prepare that one!");
			} catch (SQLException sqlEx) {
				assertEquals(currentOpenStatements,
						((com.mysql.jdbc.Connection) bareConn)
								.getActiveStatementCount());
			} finally {
				if (bareConn != null) {
					bareConn.close();
				}
			}
		}
	}

	/**
	 * Tests fix for BUG#10496 - SQLException is thrown when using property
	 * "characterSetResults"
	 */
	public void testBug10496() throws Exception {
		if (versionMeetsMinimum(5, 0, 3)) {
			Properties props = new Properties();
			props.setProperty("useUnicode", "true");
			props.setProperty("characterEncoding", "WINDOWS-31J");
			props.setProperty("characterSetResults", "WINDOWS-31J");
			getConnectionWithProps(props).close();

			props = new Properties();
			props.setProperty("useUnicode", "true");
			props.setProperty("characterEncoding", "EUC_JP");
			props.setProperty("characterSetResults", "EUC_JP");
			getConnectionWithProps(props).close();
		}
	}

	/**
	 * Tests fix for BUG#11259, autoReconnect ping causes exception on
	 * connection startup.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug11259() throws Exception {
		Connection dsConn = null;
		try {
			Properties props = new Properties();
			props.setProperty("autoReconnect", "true");
			dsConn = getConnectionWithProps(props);
		} finally {
			if (dsConn != null) {
				dsConn.close();
			}
		}
	}

	/**
	 * Tests fix for BUG#11879 -- ReplicationConnection won't switch to slave,
	 * throws "Catalog can't be null" exception.
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testBug11879() throws Exception {
		if (runMultiHostTests()) {
			Connection replConn = null;

			try {
				replConn = getMasterSlaveReplicationConnection();
				replConn.setReadOnly(true);
				replConn.setReadOnly(false);
			} finally {
				if (replConn != null) {
					replConn.close();
				}
			}
		}
	}

	/**
	 * Tests fix for BUG#11976 - maxPerformance.properties mis-spells
	 * "elideSetAutoCommits".
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug11976() throws Exception {
		if (isRunningOnJdk131()) {
			return; // test not valid on JDK-1.3.1
		}

		Properties props = new Properties();
		props.setProperty("useConfigs", "maxPerformance");

		Connection maxPerfConn = getConnectionWithProps(props);
		assertEquals(true, ((com.mysql.jdbc.Connection) maxPerfConn)
				.getElideSetAutoCommits());
	}

	/**
	 * Tests fix for BUG#12218, properties shared between master and slave with
	 * replication connection.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug12218() throws Exception {
		if (runMultiHostTests()) {
			Connection replConn = null;

			try {
				replConn = getMasterSlaveReplicationConnection();
				assertTrue(!((ReplicationConnection) replConn)
						.getMasterConnection().hasSameProperties(
								((ReplicationConnection) replConn)
										.getSlavesConnection()));
			} finally {
				if (replConn != null) {
					replConn.close();
				}
			}
		}
	}

	/**
	 * Tests fix for BUG#12229 - explainSlowQueries hangs with server-side
	 * prepared statements.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug12229() throws Exception {
		createTable("testBug12229", "(`int_field` integer )");
		this.stmt.executeUpdate("insert into testBug12229 values (123456),(1)");

		Properties props = new Properties();
		props.put("profileSQL", "true");
		props.put("slowQueryThresholdMillis", "0");
		props.put("logSlowQueries", "true");
		props.put("explainSlowQueries", "true");
		props.put("useServerPrepStmts", "true");

		Connection explainConn = getConnectionWithProps(props);

		this.pstmt = explainConn
				.prepareStatement("SELECT `int_field` FROM `testBug12229` WHERE `int_field` = ?");
		this.pstmt.setInt(1, 1);

		this.rs = this.pstmt.executeQuery();
		assertTrue(this.rs.next());

		this.rs = this.pstmt.executeQuery();
		assertTrue(this.rs.next());

		this.rs = this.pstmt.executeQuery();
		assertTrue(this.rs.next());
	}

	/**
	 * Tests fix for BUG#12752 - Cp1251 incorrectly mapped to win1251 for
	 * servers newer than 4.0.x.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug12752() throws Exception {
		Properties props = new Properties();
		props.setProperty("characterEncoding", "Cp1251");
		getConnectionWithProps(props).close();
	}

	/**
	 * Tests fix for BUG#12753, sessionVariables=....=...., doesn't work as it's
	 * tokenized incorrectly.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug12753() throws Exception {
		if (versionMeetsMinimum(4, 1)) {
			Properties props = new Properties();
			props.setProperty("sessionVariables", "sql_mode=ansi");

			Connection sessionConn = null;

			try {
				sessionConn = getConnectionWithProps(props);

				String sqlMode = getMysqlVariable(sessionConn, "sql_mode");
				assertTrue(sqlMode.indexOf("ANSI") != -1);
			} finally {
				if (sessionConn != null) {
					sessionConn.close();
					sessionConn = null;
				}
			}
		}
	}

	/**
	 * Tests fix for BUG#13048 - maxQuerySizeToLog is not respected.
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testBug13048() throws Exception {

		Connection profileConn = null;
		PrintStream oldErr = System.err;

		try {
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			System.setErr(new PrintStream(bOut));

			Properties props = new Properties();
			props.setProperty("profileSQL", "true");
			props.setProperty("maxQuerySizeToLog", "2");
			props.setProperty("logger", "com.mysql.jdbc.log.StandardLogger");

			profileConn = getConnectionWithProps(props);

			StringBuffer queryBuf = new StringBuffer("SELECT '");

			for (int i = 0; i < 500; i++) {
				queryBuf.append("a");
			}

			queryBuf.append("'");

			this.rs = profileConn.createStatement().executeQuery(
					queryBuf.toString());
			this.rs.close();

			String logString = new String(bOut.toString("ISO8859-1"));
			assertTrue(logString.indexOf("... (truncated)") != -1);

			bOut = new ByteArrayOutputStream();
			System.setErr(new PrintStream(bOut));

			this.rs = profileConn.prepareStatement(queryBuf.toString())
					.executeQuery();
			logString = new String(bOut.toString("ISO8859-1"));

			assertTrue(logString.indexOf("... (truncated)") != -1);
		} finally {
			System.setErr(oldErr);

			if (profileConn != null) {
				profileConn.close();
			}

			if (this.rs != null) {
				ResultSet toClose = this.rs;
				this.rs = null;
				toClose.close();
			}
		}
	}

	/**
	 * Tests fix for BUG#13453 - can't use & or = in URL configuration values
	 * (we now allow you to use www-form-encoding).
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testBug13453() throws Exception {
		StringBuffer urlBuf = new StringBuffer(dbUrl);

		if (dbUrl.indexOf('?') == -1) {
			urlBuf.append('?');
		} else {
			urlBuf.append('&');
		}

		urlBuf.append("sessionVariables=@testBug13453='%25%26+%3D'");

		Connection encodedConn = null;

		try {
			encodedConn = DriverManager.getConnection(urlBuf.toString(), null);

			this.rs = encodedConn.createStatement().executeQuery(
					"SELECT @testBug13453");
			assertTrue(this.rs.next());
			assertEquals("%& =", this.rs.getString(1));
		} finally {
			if (this.rs != null) {
				this.rs.close();
				this.rs = null;
			}

			if (encodedConn != null) {
				encodedConn.close();
			}
		}
	}

	/**
	 * Tests fix for BUG#15065 - Usage advisor complains about unreferenced
	 * columns, even though they've been referenced.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug15065() throws Exception {
		if (isRunningOnJdk131()) {
			return; // test not valid on JDK-1.3.1
		}

		createTable("testBug15065", "(field1 int)");

		this.stmt.executeUpdate("INSERT INTO testBug15065 VALUES (1)");

		Connection advisorConn = null;
		Statement advisorStmt = null;

		try {
			Properties props = new Properties();
			props.setProperty("useUsageAdvisor", "true");
			props.setProperty("logger", "com.mysql.jdbc.log.StandardLogger");

			advisorConn = getConnectionWithProps(props);
			advisorStmt = advisorConn.createStatement();

			Method[] getMethods = ResultSet.class.getMethods();

			PrintStream oldErr = System.err;

			try {
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				System.setErr(new PrintStream(bOut));
				
				HashMap methodsToSkipMap = new HashMap();
				
				// Needs an actual URL
				methodsToSkipMap.put("getURL", null);
				
				// Java6 JDBC4.0 methods we don't implement
				methodsToSkipMap.put("getNCharacterStream", null);
				methodsToSkipMap.put("getNClob", null);
				methodsToSkipMap.put("getNString", null);
				methodsToSkipMap.put("getRowId", null);
				methodsToSkipMap.put("getSQLXML", null);
				
				for (int j = 0; j < 2; j++) {
					for (int i = 0; i < getMethods.length; i++) {
						String methodName = getMethods[i].getName();

						if (methodName.startsWith("get")
								&& !methodsToSkipMap.containsKey(methodName)) {
							Class[] parameterTypes = getMethods[i]
									.getParameterTypes();

							if (parameterTypes.length == 1
									&& parameterTypes[0] == Integer.TYPE) {
								if (j == 0) {
									this.rs = advisorStmt
											.executeQuery("SELECT COUNT(*) FROM testBug15065");
								} else {
									this.rs = advisorConn
											.prepareStatement(
													"SELECT COUNT(*) FROM testBug15065")
											.executeQuery();
								}

								this.rs.next();

								try {

									getMethods[i].invoke(this.rs,
											new Object[] { new Integer(1) });
								} catch (InvocationTargetException invokeEx) {
									// we don't care about bad values, just that
									// the
									// column gets "touched"
									if (!invokeEx
											.getCause()
											.getClass()
											.isAssignableFrom(
													java.sql.SQLException.class)
											&& !invokeEx
													.getCause()
													.getClass()
													.getName()
													.equals(
															"com.mysql.jdbc.NotImplemented")) {
										throw invokeEx;
									}
								}

								this.rs.close();
								this.rs = null;
							}
						}
					}
				}

				String logOut = bOut.toString("ISO8859-1");

				if (logOut.indexOf(".Level") != -1) {
					return; // we ignore for warnings
				}

				assertTrue("Usage advisor complained about columns:\n\n"
						+ logOut, logOut.indexOf("columns") == -1);
			} finally {
				System.setErr(oldErr);
			}
		} finally {
			if (advisorConn != null) {
				advisorConn.close();
			}
		}
	}
	

	/**
	 * Tests fix for BUG#15544, no "dos" character set in MySQL > 4.1.0
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	public void testBug15544() throws Exception {
		Properties props = new Properties();
		props.setProperty("characterEncoding", "Cp437");
		Connection dosConn = null;

		try {
			dosConn = getConnectionWithProps(props);
		} finally {
			if (dosConn != null) {
				dosConn.close();
			}
		}
	}

	public void testCSC5765() throws Exception {
		if (isRunningOnJdk131()) {
			return; // test not valid on JDK-1.3.1
		}

		Properties props = new Properties();
		props.setProperty("useUnicode", "true");
		props.setProperty("characterEncoding", "utf8");
		props.setProperty("characterSetResults", "utf8");
		props.setProperty("connectionCollation", "utf8_bin");

		Connection utf8Conn = null;

		try {
			utf8Conn = getConnectionWithProps(props);
			this.rs = utf8Conn.createStatement().executeQuery(
					"SHOW VARIABLES LIKE 'character_%'");
			while (this.rs.next()) {
				System.out.println(this.rs.getString(1) + " = "
						+ this.rs.getString(2));
			}

			this.rs = utf8Conn.createStatement().executeQuery(
					"SHOW VARIABLES LIKE 'collation_%'");
			while (this.rs.next()) {
				System.out.println(this.rs.getString(1) + " = "
						+ this.rs.getString(2));
			}
		} finally {
			if (utf8Conn != null) {
				utf8Conn.close();
			}
		}
	}

	private Connection getMasterSlaveReplicationConnection()
			throws SQLException {

		Connection replConn = new ReplicationDriver().connect(
				getMasterSlaveUrl(), getMasterSlaveProps());

		return replConn;
	}

	protected String getMasterSlaveUrl() throws SQLException {
		StringBuffer urlBuf = new StringBuffer("jdbc:mysql://");
		Properties defaultProps = getPropertiesFromTestsuiteUrl();
		String hostname = defaultProps
				.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY);

		int colonIndex = hostname.indexOf(":");

		String portNumber = "3306";

		if (colonIndex != -1 && !hostname.startsWith(":")) {
			portNumber = hostname.substring(colonIndex + 1);
			hostname = hostname.substring(0, colonIndex);
		} else if (hostname.startsWith(":")) {
			portNumber = hostname.substring(1);
			hostname = "localhost";
		} else {
			portNumber = defaultProps
					.getProperty(NonRegisteringDriver.PORT_PROPERTY_KEY);
		}

		for (int i = 0; i < 2; i++) {
			urlBuf.append(hostname);
			urlBuf.append(":");
			urlBuf.append(portNumber);

			if (i == 0) {
				urlBuf.append(",");
			}
		}
		urlBuf.append("/");

		return urlBuf.toString();
	}

	protected Properties getMasterSlaveProps() throws SQLException {
		Properties props = getPropertiesFromTestsuiteUrl();

		props.remove(NonRegisteringDriver.HOST_PROPERTY_KEY);
		props.remove(NonRegisteringDriver.PORT_PROPERTY_KEY);

		return props;
	}

	/**
	 * Tests fix for BUG#15570 - ReplicationConnection incorrectly copies state,
	 * doesn't transfer connection context correctly when transitioning between
	 * the same read-only states.
	 * 
	 * (note, this test will fail if the test user doesn't have permission to
	 * "USE 'mysql'".
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testBug15570() throws Exception {
		Connection replConn = null;

		try {
			replConn = getMasterSlaveReplicationConnection();

			int masterConnectionId = Integer
					.parseInt(getSingleIndexedValueWithQuery(replConn, 1,
							"SELECT CONNECTION_ID()").toString());

			replConn.setReadOnly(false);

			assertEquals(masterConnectionId, Integer
					.parseInt(getSingleIndexedValueWithQuery(replConn, 1,
							"SELECT CONNECTION_ID()").toString()));

			String currentCatalog = replConn.getCatalog();

			replConn.setCatalog(currentCatalog);
			assertEquals(currentCatalog, replConn.getCatalog());

			replConn.setReadOnly(true);

			int slaveConnectionId = Integer
					.parseInt(getSingleIndexedValueWithQuery(replConn, 1,
							"SELECT CONNECTION_ID()").toString());

			// The following test is okay for now, as the chance
			// of MySQL wrapping the connection id counter during our
			// testsuite is very small.

			assertTrue("Slave id " + slaveConnectionId
					+ " is not newer than master id " + masterConnectionId,
					slaveConnectionId > masterConnectionId);

			assertEquals(currentCatalog, replConn.getCatalog());

			String newCatalog = "mysql";

			replConn.setCatalog(newCatalog);
			assertEquals(newCatalog, replConn.getCatalog());

			replConn.setReadOnly(true);
			assertEquals(newCatalog, replConn.getCatalog());

			replConn.setReadOnly(false);
			assertEquals(masterConnectionId, Integer
					.parseInt(getSingleIndexedValueWithQuery(replConn, 1,
							"SELECT CONNECTION_ID()").toString()));
		} finally {
			if (replConn != null) {
				replConn.close();
			}
		}
	}

	/**
	 * Tests bug where downed slave caused round robin load balance not to
	 * cycle back to first host in the list.
	 * 
	 * @throws Exception
	 *             if the test fails...Note, test is timing-dependent, but
	 *             should work in most cases.
	 */
	public void testBug23281() throws Exception {
		Properties props = new Driver().parseURL(BaseTestCase.dbUrl, null);
		props.setProperty("autoReconnect", "false");
		props.setProperty("roundRobinLoadBalance", "true");
		props.setProperty("failoverReadOnly", "false");
		
		if (!isRunningOnJdk131()) {
			props.setProperty("connectTimeout", "5000");
		}
		
		// Re-build the connection information
		int firstIndexOfHost = BaseTestCase.dbUrl.indexOf("//") + 2;
		int lastIndexOfHost = BaseTestCase.dbUrl.indexOf("/", firstIndexOfHost);
	
		String hostPortPair = BaseTestCase.dbUrl.substring(firstIndexOfHost,
				lastIndexOfHost);
	
		StringTokenizer st = new StringTokenizer(hostPortPair, ":");
	
		String host = null;
		String port = null;
	
		if (st.hasMoreTokens()) {
			String possibleHostOrPort = st.nextToken();
	
			if (Character.isDigit(possibleHostOrPort.charAt(0)) && 
					(possibleHostOrPort.indexOf(".") == -1 /* IPV4 */)  &&
					(possibleHostOrPort.indexOf("::") == -1 /* IPV6 */)) {
				port = possibleHostOrPort;
				host = "localhost";
			} else {
				host = possibleHostOrPort;
			}
		}
	
		if (st.hasMoreTokens()) {
			port = st.nextToken();
		}
	
		if (host == null) {
			host = "";
		}
	
		if (port == null) {
			port = "3306";
		}
	
		StringBuffer newHostBuf = new StringBuffer();
		
		newHostBuf.append(host);
		if (port != null) {
			newHostBuf.append(":");
			newHostBuf.append(port);
		}
	
		newHostBuf.append(",");
		//newHostBuf.append(host);
		newHostBuf.append("192.0.2.1"); // non-exsitent machine from RFC3330 test network
		newHostBuf.append(":65532"); // make sure the slave fails
		
		props.remove("PORT");
		props.remove("HOST");
	
		Connection failoverConnection = null;
	
		try {
			failoverConnection = getConnectionWithProps("jdbc:mysql://"
					+ newHostBuf.toString() + "/", props);
		
			String originalConnectionId = getSingleIndexedValueWithQuery(
					failoverConnection, 1, "SELECT CONNECTION_ID()").toString();
			
			System.out.println(originalConnectionId);
			
			Connection nextConnection = getConnectionWithProps("jdbc:mysql://"
					+ newHostBuf.toString() + "/", props);
			
			String nextId = getSingleIndexedValueWithQuery(
					nextConnection, 1, "SELECT CONNECTION_ID()").toString();
			
			System.out.println(nextId);
			
		} finally {
			if (failoverConnection != null) {
				failoverConnection.close();
			}
		}
	}
	
	/**
	 * Tests to insure proper behavior for BUG#24706.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug24706() throws Exception {
		if (!versionMeetsMinimum(5, 0)) {
			return; // server status isn't there to support this feature
		}
		
		Properties props = new Properties();
		props.setProperty("elideSetAutoCommits", "true");
		props.setProperty("logger", "StandardLogger");
		props.setProperty("profileSQL", "true");
		Connection c = null;
		
		StringBuffer logBuf = new StringBuffer();
		
		StandardLogger.bufferedLog = logBuf;
		
		try {
			c = getConnectionWithProps(props);
			c.setAutoCommit(true);
			c.createStatement().execute("SELECT 1");
			c.setAutoCommit(true);
			c.setAutoCommit(false);
			c.createStatement().execute("SELECT 1");
			c.setAutoCommit(false);
			
			// We should only see _one_ "set autocommit=" sent to the server
			
			String log = logBuf.toString();
			int searchFrom = 0;
			int count = 0;
			int found = 0;
			
			while ((found = log.indexOf("SET autocommit=", searchFrom)) != -1) {
				searchFrom =  found + 1;
				count++;
			}
			
			// The SELECT doesn't actually start a transaction, so being pedantic the
			// driver issues SET autocommit=0 again in this case.
			assertEquals(2, count);
		} finally {
			StandardLogger.bufferedLog = null;
			
			if (c != null) {
				c.close();
			}
			
		}
	}
	
	/**
	 * Tests fix for BUG#25514 - Timer instance used for Statement.setQueryTimeout()
	 * created per-connection, rather than per-VM, causing memory leak.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug25514() throws Exception {

		for (int i = 0; i < 10; i++) {
			getConnectionWithProps((Properties)null).close();
		}
		
		ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();

		while (root.getParent() != null) {
	        root = root.getParent();
	    }

		int numThreadsNamedTimer = findNamedThreadCount(root, "Timer");

		if (numThreadsNamedTimer == 0) {
			numThreadsNamedTimer = findNamedThreadCount(root, "MySQL Statement Cancellation Timer");
		}
		
		// Notice that this seems impossible to test on JDKs prior to 1.5, as there is no
		// reliable way to find the TimerThread, so we have to rely on new JDKs for this 
		// test.
		assertTrue("More than one timer for cancel was created", numThreadsNamedTimer <= 1);
	}
	
	private int findNamedThreadCount(ThreadGroup group, String nameStart) {
		
		int count = 0;
		
        int numThreads = group.activeCount();
        Thread[] threads = new Thread[numThreads*2];
        numThreads = group.enumerate(threads, false);
    
        for (int i=0; i<numThreads; i++) {
            if (threads[i].getName().startsWith(nameStart)) {
            	count++;
            }
        }

        int numGroups = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups*2];
        numGroups = group.enumerate(groups, false);
    
        for (int i=0; i<numGroups; i++) {
        	count += findNamedThreadCount(groups[i], nameStart);
        }

        return count;
	}
	
	/**
	 * Ensures that we don't miss getters/setters for driver properties in
	 * ConnectionProperties so that names given in documentation work with 
	 * DataSources which will use JavaBean-style names and reflection to 
	 * set the values (and often fail silently! when the method isn't available).
	 * 
	 * @throws Exception
	 */
	public void testBug23626() throws Exception {
		Class clazz = this.conn.getClass();
		
		DriverPropertyInfo[] dpi = new NonRegisteringDriver().getPropertyInfo(dbUrl, null);
		StringBuffer missingSettersBuf = new StringBuffer();
		StringBuffer missingGettersBuf = new StringBuffer();
		
		Class[][] argTypes = {new Class[] { String.class }, new Class[] {Integer.TYPE}, new Class[] {Long.TYPE}, new Class[] {Boolean.TYPE}};
		
		for (int i = 0; i < dpi.length; i++) {
			
			String propertyName = dpi[i].name;
		
			if (propertyName.equals("HOST") || propertyName.equals("PORT") 
					|| propertyName.equals("DBNAME") || propertyName.equals("user") ||
					propertyName.equals("password")) {
				continue;
			}
					
			StringBuffer mutatorName = new StringBuffer("set");
			mutatorName.append(Character.toUpperCase(propertyName.charAt(0)));
			mutatorName.append(propertyName.substring(1));
				
			StringBuffer accessorName = new StringBuffer("get");
			accessorName.append(Character.toUpperCase(propertyName.charAt(0)));
			accessorName.append(propertyName.substring(1));
			
			try {
				clazz.getMethod(accessorName.toString(), null);
			} catch (NoSuchMethodException nsme) {
				missingGettersBuf.append(accessorName.toString());
				missingGettersBuf.append("\n");
			}
			
			boolean foundMethod = false;
			
			for (int j = 0; j < argTypes.length; j++) {
				try {
					clazz.getMethod(mutatorName.toString(), argTypes[j]);
					foundMethod = true;
					break;
				} catch (NoSuchMethodException nsme) {
					
				}
			}
			
			if (!foundMethod) {
				missingSettersBuf.append(mutatorName);
				missingSettersBuf.append("\n");
			}
		}
		
		assertEquals("Missing setters for listed configuration properties.", "", missingSettersBuf.toString());
		assertEquals("Missing getters for listed configuration properties.", "", missingSettersBuf.toString());
	}
	
	/**
	 * Tests fix for BUG#25545 - Client flags not sent correctly during handshake
	 * when using SSL.
	 * 
	 * Requires test certificates from testsuite/ssl-test-certs to be installed
	 * on the server being tested.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testBug25545() throws Exception {
		if (!versionMeetsMinimum(5, 0)) {
			return;
		}
		
		if (isRunningOnJdk131()) {
			return;
		}
	
		createProcedure("testBug25545", "() BEGIN SELECT 1; END");
		
		String trustStorePath = "src/testsuite/ssl-test-certs/test-cert-store";
		
		System.setProperty("javax.net.ssl.keyStore", trustStorePath);
		System.setProperty("javax.net.ssl.keyStorePassword","password");
		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword","password");
		
		
		Connection sslConn = null;
		
		try {
			Properties props = new Properties();
			props.setProperty("useSSL", "true");
			props.setProperty("requireSSL", "true");
			
			sslConn = getConnectionWithProps(props);
			sslConn.prepareCall("{ call testBug25545()}").execute();
		} finally {
			if (sslConn != null) {
				sslConn.close();
			}
		}
	}
	
	/**
	 * Tests fix for BUG#27655 - getTransactionIsolation() uses
	 * "SHOW VARIABLES LIKE" which is very inefficient on MySQL-5.0+
	 * 
	 * @throws Exception
	 */
	public void testBug27655() throws Exception {
		StringBuffer logBuf = new StringBuffer();
		Properties props = new Properties();
		props.setProperty("profileSQL", "true");
		props.setProperty("logger", "StandardLogger");
		StandardLogger.bufferedLog = logBuf;
		
		Connection loggedConn = null;
		
		try {
			loggedConn = getConnectionWithProps(props);
			loggedConn.getTransactionIsolation();
			
			if (versionMeetsMinimum(4, 0, 3)) {
				assertEquals(-1, logBuf.toString().indexOf("SHOW VARIABLES LIKE 'tx_isolation'"));
			}
		} finally {
			if (loggedConn != null) {
				loggedConn.close();
			}
		}
	}
	
	/**
	 * Tests fix for issue where a failed-over connection would let
	 * an application call setReadOnly(false), when that call 
	 * should be ignored until the connection is reconnected to a 
	 * writable master.
	 * 
	 * @throws Exception if the test fails.
	 */
	public void testFailoverReadOnly() throws Exception {
		Properties props = getMasterSlaveProps();
		props.setProperty("autoReconnect", "true");
		
		
		Connection failoverConn = null;
		
		
		Statement failoverStmt = 
			null;
		
		try {
			failoverConn = getConnectionWithProps(getMasterSlaveUrl(), props);

			((com.mysql.jdbc.Connection)failoverConn).setPreferSlaveDuringFailover(true);
			
			failoverStmt = failoverConn.createStatement();
			
			String masterConnectionId = getSingleIndexedValueWithQuery(failoverConn, 1, "SELECT connection_id()").toString();
			
			
			this.stmt.execute("KILL " + masterConnectionId);
			
			// die trying, so we get the next host
			for (int i = 0; i < 100; i++) {
				try {
					failoverStmt.executeQuery("SELECT 1");
				} catch (SQLException sqlEx) {
					break;
				}
			}
			
			String slaveConnectionId = getSingleIndexedValueWithQuery(failoverConn, 1, "SELECT connection_id()").toString();
			
			assertTrue("Didn't get a new physical connection",
					!masterConnectionId.equals(slaveConnectionId));
			
			failoverConn.setReadOnly(false); // this should be ignored
			
			assertTrue(failoverConn.isReadOnly());
			
			((com.mysql.jdbc.Connection)failoverConn).setPreferSlaveDuringFailover(false);
			
			this.stmt.execute("KILL " + slaveConnectionId); // we can't issue this on our own connection :p
			
			// die trying, so we get the next host
			for (int i = 0; i < 100; i++) {
				try {
					failoverStmt.executeQuery("SELECT 1");
				} catch (SQLException sqlEx) {
					break;
				}
			}
			
			String newMasterId = getSingleIndexedValueWithQuery(failoverConn, 1, "SELECT connection_id()").toString();
			
			assertTrue("Didn't get a new physical connection",
					!slaveConnectionId.equals(newMasterId));
			
			failoverConn.setReadOnly(false);
			
			assertTrue(!failoverConn.isReadOnly());
		} finally {
			if (failoverStmt != null) {
				failoverStmt.close();
			}
			
			if (failoverConn != null) {
				failoverConn.close();
			}
		}
	}
	
	public void testBug29852() throws Exception {
    	Connection lbConn = getLoadBalancedConnection();
    	assertTrue(!lbConn.getClass().getName().startsWith("com.mysql.jdbc"));
    	lbConn.close();
    }

	private Connection getLoadBalancedConnection() throws SQLException {
		int indexOfHostStart = dbUrl.indexOf("://") + 3;
    	int indexOfHostEnd = dbUrl.indexOf("/", indexOfHostStart);
    	
    	String backHalf = dbUrl.substring(indexOfHostStart, indexOfHostEnd);
    	
    	if (backHalf.length() == 0) {
    		backHalf = "localhost:3306";
    	}
    	
    	String dbAndConfigs = dbUrl.substring(indexOfHostEnd);
    	
    	Connection lbConn = DriverManager.getConnection("jdbc:mysql:loadbalance://" + backHalf + "," + backHalf + dbAndConfigs);
		return lbConn;
	}
	
	/**
	 * Test of a new feature to fix BUG 22643, specifying a
	 * "validation query" in your connection pool that starts
	 * with "slash-star ping slash-star" _exactly_ will cause the driver to " +
	 * instead send a ping to the server (much lighter weight), and when using
	 * a ReplicationConnection or a LoadBalancedConnection, will send
	 * the ping across all active connections.
	 * 
	 * @throws Exception
	 */
	public void testBug22643() throws Exception {
		checkPingQuery(this.conn);
		
		Connection replConnection = getMasterSlaveReplicationConnection();
		
		try {
			checkPingQuery(replConnection);
		} finally {
			if (replConnection != null) {
				replConnection.close();
			}
		}
		
		Connection lbConn = getLoadBalancedConnection();
		
		try {
			checkPingQuery(lbConn);
		} finally {
			if (lbConn != null) {
				lbConn.close();
			}
		}
	}

	private void checkPingQuery(Connection c) throws SQLException {
		// Yes, I know we're sending 2, and looking for 1
		// that's part of the test, since we don't _really_
		// send the query to the server!
		String aPingQuery = "/* ping */ SELECT 2";
		Statement pingStmt = c.createStatement();
		PreparedStatement pingPStmt = null;
		
		try {
			this.rs = pingStmt.executeQuery(aPingQuery);
			assertTrue(this.rs.next());
			assertEquals(this.rs.getInt(1), 1);
			
			assertTrue(pingStmt.execute(aPingQuery));
			this.rs = pingStmt.getResultSet();
			assertTrue(this.rs.next());
			assertEquals(this.rs.getInt(1), 1);
			
			pingPStmt = c.prepareStatement(aPingQuery);
			
			assertTrue(pingPStmt.execute());
			this.rs = pingPStmt.getResultSet();
			assertTrue(this.rs.next());
			assertEquals(this.rs.getInt(1), 1);
			
			this.rs = pingPStmt.executeQuery();
			assertTrue(this.rs.next());
			assertEquals(this.rs.getInt(1), 1);
		} finally {
			closeMemberJDBCResources();
		}
	}
}