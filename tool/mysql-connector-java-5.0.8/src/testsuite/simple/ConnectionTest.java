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
package testsuite.simple;

import testsuite.BaseTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.mysql.jdbc.ConnectionPropertiesTransform;
import com.mysql.jdbc.Driver;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.log.StandardLogger;

/**
 * Tests java.sql.Connection functionality ConnectionTest.java,v 1.1 2002/12/06
 * 22:01:05 mmatthew Exp
 * 
 * @author Mark Matthews
 */
public class ConnectionTest extends BaseTestCase {
	/**
	 * Constructor for ConnectionTest.
	 * 
	 * @param name
	 *            the name of the test to run
	 */
	public ConnectionTest(String name) {
		super(name);
	}

	/**
	 * Runs all test cases in this test suite
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ConnectionTest.class);
	}

	/**
	 * Tests catalog functionality
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void testCatalog() throws Exception {
		String currentCatalog = this.conn.getCatalog();
		this.conn.setCatalog(currentCatalog);
		assertTrue(currentCatalog.equals(this.conn.getCatalog()));
	}

	/**
	 * Tests a cluster connection for failover, requires a two-node cluster URL
	 * specfied in com.mysql.jdbc.testsuite.ClusterUrl system proeprty.
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void testClusterConnection() throws Exception {
		String url = System.getProperty("com.mysql.jdbc.testsuite.ClusterUrl");

		if ((url != null) && (url.length() > 0)) {
			Object versionNumObj = getSingleValueWithQuery("SHOW VARIABLES LIKE 'version'");

			if ((versionNumObj != null)
					&& (versionNumObj.toString().indexOf("cluster") != -1)) {
				Connection clusterConn = null;
				Statement clusterStmt = null;

				try {
					clusterConn = new NonRegisteringDriver().connect(url, null);

					clusterStmt = clusterConn.createStatement();
					clusterStmt
							.executeQuery("DROP TABLE IF EXISTS testClusterConn");
					clusterStmt
							.executeQuery("CREATE TABLE testClusterConn (field1 INT) TYPE=ndbcluster");
					clusterStmt
							.executeQuery("INSERT INTO testClusterConn VALUES (1)");

					clusterConn.setAutoCommit(false);

					clusterStmt.executeQuery("SELECT * FROM testClusterConn");
					clusterStmt
							.executeUpdate("UPDATE testClusterConn SET field1=4");

					// Kill the connection
					String connectionId = getSingleValueWithQuery(
							"SELECT CONNECTION_ID()").toString();

					System.out
							.println("Please kill the MySQL server now and press return...");
					System.in.read();

					System.out.println("Waiting for TCP/IP timeout...");
					Thread.sleep(10);

					System.out.println("Attempting auto reconnect");

					try {
						clusterConn.setAutoCommit(true);
						clusterConn.setAutoCommit(false);
					} catch (SQLException sqlEx) {
						System.out.println(sqlEx);
					}

					//
					// Test that this 'new' connection is not read-only
					//
					clusterStmt
							.executeUpdate("UPDATE testClusterConn SET field1=5");

					ResultSet rs = clusterStmt
							.executeQuery("SELECT * FROM testClusterConn WHERE field1=5");

					assertTrue("One row should be returned", rs.next());
				} finally {
					if (clusterStmt != null) {
						clusterStmt
								.executeQuery("DROP TABLE IF EXISTS testClusterConn");
						clusterStmt.close();
					}

					if (clusterConn != null) {
						clusterConn.close();
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void testDeadlockDetection() throws Exception {
		try {
			this.rs = this.stmt
					.executeQuery("SHOW VARIABLES LIKE 'innodb_lock_wait_timeout'");
			this.rs.next();

			int timeoutSecs = this.rs.getInt(2);

			this.stmt.executeUpdate("DROP TABLE IF EXISTS t1");
			this.stmt
					.executeUpdate("CREATE TABLE t1 (id INTEGER, x INTEGER) TYPE=INNODB");
			this.stmt.executeUpdate("INSERT INTO t1 VALUES(0, 0)");
			this.conn.setAutoCommit(false);
			this.conn.createStatement().executeQuery(
					"SELECT * FROM t1 WHERE id=0 FOR UPDATE");

			Properties props = new Properties();
			props.setProperty("includeInnodbStatusInDeadlockExceptions", "true");
			
			Connection deadlockConn = getConnectionWithProps(props);
			deadlockConn.setAutoCommit(false);

			// The following query should hang because con1 is locking the page
			deadlockConn.createStatement().executeUpdate(
					"UPDATE t1 SET x=2 WHERE id=0");
			deadlockConn.commit();

			Thread.sleep(timeoutSecs * 2 * 1000);
		} catch (SQLException sqlEx) {
			System.out
					.println("Caught SQLException due to deadlock/lock timeout");
			System.out.println("SQLState: " + sqlEx.getSQLState());
			System.out.println("Vendor error: " + sqlEx.getErrorCode());
			System.out.println("Message: " + sqlEx.getMessage());

			//
			// Check whether the driver thinks it really is deadlock...
			//
			assertTrue(SQLError.SQL_STATE_DEADLOCK.equals(sqlEx.getSQLState()));
			assertTrue(sqlEx.getErrorCode() == 1205);
			// Make sure INNODB Status is getting dumped into error message
			assertTrue(sqlEx.getMessage().indexOf("INNODB MONITOR") != -1);
		} finally {
			this.conn.setAutoCommit(true);
			this.stmt.executeUpdate("DROP TABLE IF EXISTS t1");
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void testCharsets() throws Exception {
		if (versionMeetsMinimum(4, 1)) {
			try {
				Properties props = new Properties();
				props.setProperty("useUnicode", "true");
				props.setProperty("characterEncoding", "UTF-8");

				Connection utfConn = getConnectionWithProps(props);

				this.stmt = utfConn.createStatement();

				this.stmt.executeUpdate("DROP TABLE IF EXISTS t1");
				// this.stmt.executeUpdate("SET CHARACTER SET latin1");

				this.stmt.executeUpdate("CREATE TABLE t1 ("
						+ "comment CHAR(32) ASCII NOT NULL,"
						+ "koi8_ru_f CHAR(32) CHARACTER SET koi8r NOT NULL"
						+ ") CHARSET=latin5");

				this.stmt
						.executeUpdate("ALTER TABLE t1 CHANGE comment comment CHAR(32) CHARACTER SET latin2 NOT NULL");
				this.stmt
						.executeUpdate("ALTER TABLE t1 ADD latin5_f CHAR(32) NOT NULL");
				this.stmt.executeUpdate("ALTER TABLE t1 CHARSET=latin2");
				this.stmt
						.executeUpdate("ALTER TABLE t1 ADD latin2_f CHAR(32) NOT NULL");
				this.stmt
						.executeUpdate("ALTER TABLE t1 DROP latin2_f, DROP latin5_f");

				this.stmt
						.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment) VALUES ('a','LAT SMALL A')");
				/*
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('b','LAT SMALL B')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('c','LAT SMALL C')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('d','LAT SMALL D')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('e','LAT SMALL E')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('f','LAT SMALL F')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('g','LAT SMALL G')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('h','LAT SMALL H')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('i','LAT SMALL I')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('j','LAT SMALL J')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('k','LAT SMALL K')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('l','LAT SMALL L')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('m','LAT SMALL M')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('n','LAT SMALL N')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('o','LAT SMALL O')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('p','LAT SMALL P')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('q','LAT SMALL Q')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('r','LAT SMALL R')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('s','LAT SMALL S')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('t','LAT SMALL T')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('u','LAT SMALL U')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('v','LAT SMALL V')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('w','LAT SMALL W')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('x','LAT SMALL X')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('y','LAT SMALL Y')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('z','LAT SMALL Z')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('A','LAT CAPIT A')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('B','LAT CAPIT B')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('C','LAT CAPIT C')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('D','LAT CAPIT D')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('E','LAT CAPIT E')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('F','LAT CAPIT F')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('G','LAT CAPIT G')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('H','LAT CAPIT H')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('I','LAT CAPIT I')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('J','LAT CAPIT J')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('K','LAT CAPIT K')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('L','LAT CAPIT L')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('M','LAT CAPIT M')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('N','LAT CAPIT N')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('O','LAT CAPIT O')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('P','LAT CAPIT P')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('Q','LAT CAPIT Q')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('R','LAT CAPIT R')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('S','LAT CAPIT S')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('T','LAT CAPIT T')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('U','LAT CAPIT U')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('V','LAT CAPIT V')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('W','LAT CAPIT W')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('X','LAT CAPIT X')"); this.stmt.executeUpdate("INSERT
				 * INTO t1 (koi8_ru_f,comment) VALUES ('Y','LAT CAPIT Y')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES ('Z','LAT CAPIT Z')");
				 */

				String cyrillicSmallA = "\u0430";
				this.stmt
						.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment) VALUES ('"
								+ cyrillicSmallA + "','CYR SMALL A')");

				/*
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL BE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL VE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL GE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL DE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL IE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL IO')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL ZHE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL ZE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL I')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL KA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL EL')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL EM')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL EN')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL O')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL PE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL ER')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL ES')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL TE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL U')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL EF')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL HA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL TSE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL CHE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL SHA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL SCHA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL HARD SIGN')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL YERU')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL SOFT SIGN')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL E')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL YU')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR SMALL YA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT A')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT BE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT VE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT GE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT DE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT IE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT IO')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT ZHE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT ZE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT I')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT KA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT EL')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT EM')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT EN')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT O')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT PE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT ER')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT ES')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT TE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT U')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT EF')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT HA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT TSE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT CHE')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT SHA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT SCHA')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT HARD SIGN')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT YERU')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT SOFT SIGN')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT E')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT YU')");
				 * this.stmt.executeUpdate("INSERT INTO t1 (koi8_ru_f,comment)
				 * VALUES (_koi8r'?��','CYR CAPIT YA')");
				 */

				this.stmt
						.executeUpdate("ALTER TABLE t1 ADD utf8_f CHAR(32) CHARACTER SET utf8 NOT NULL");
				this.stmt
						.executeUpdate("UPDATE t1 SET utf8_f=CONVERT(koi8_ru_f USING utf8)");
				this.stmt.executeUpdate("SET CHARACTER SET koi8r");
				// this.stmt.executeUpdate("SET CHARACTER SET UTF8");
				this.rs = this.stmt.executeQuery("SELECT * FROM t1");

				ResultSetMetaData rsmd = this.rs.getMetaData();

				int numColumns = rsmd.getColumnCount();

				for (int i = 0; i < numColumns; i++) {
					System.out.print(rsmd.getColumnName(i + 1));
					System.out.print("\t\t");
				}

				System.out.println();

				while (this.rs.next()) {
					System.out.println(this.rs.getString(1) + "\t\t"
							+ this.rs.getString(2) + "\t\t"
							+ this.rs.getString(3));

					if (this.rs.getString(1).equals("CYR SMALL A")) {
						this.rs.getString(2);
					}
				}

				System.out.println();

				this.stmt.executeUpdate("SET NAMES utf8");
				this.rs = this.stmt.executeQuery("SELECT _koi8r 0xC1;");

				rsmd = this.rs.getMetaData();

				numColumns = rsmd.getColumnCount();

				for (int i = 0; i < numColumns; i++) {
					System.out.print(rsmd.getColumnName(i + 1));
					System.out.print("\t\t");
				}

				System.out.println();

				while (this.rs.next()) {
					System.out.println(this.rs.getString(1).equals("\u0430")
							+ "\t\t");
					System.out
							.println(new String(this.rs.getBytes(1), "KOI8_R"));

				}

				char[] c = new char[] { 0xd0b0 };

				System.out.println(new String(c));
				System.out.println("\u0430");
			} finally {
				// this.stmt.executeUpdate("DROP TABLE IF EXISTS t1");
			}
		}
	}

	/**
	 * Tests isolation level functionality
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void testIsolationLevel() throws Exception {
		if (versionMeetsMinimum(4, 0)) {
			String[] isoLevelNames = new String[] {
					"Connection.TRANSACTION_NONE",
					"Connection.TRANSACTION_READ_COMMITTED",
					"Connection.TRANSACTION_READ_UNCOMMITTED",
					"Connection.TRANSACTION_REPEATABLE_READ",
					"Connection.TRANSACTION_SERIALIZABLE" };

			int[] isolationLevels = new int[] { Connection.TRANSACTION_NONE,
					Connection.TRANSACTION_READ_COMMITTED,
					Connection.TRANSACTION_READ_UNCOMMITTED,
					Connection.TRANSACTION_REPEATABLE_READ,
					Connection.TRANSACTION_SERIALIZABLE };

			DatabaseMetaData dbmd = this.conn.getMetaData();

			for (int i = 0; i < isolationLevels.length; i++) {
				if (dbmd.supportsTransactionIsolationLevel(isolationLevels[i])) {
					this.conn.setTransactionIsolation(isolationLevels[i]);

					assertTrue(
							"Transaction isolation level that was set ("
									+ isoLevelNames[i]
									+ ") was not returned, nor was a more restrictive isolation level used by the server",
							this.conn.getTransactionIsolation() == isolationLevels[i]
									|| this.conn.getTransactionIsolation() > isolationLevels[i]);
				}
			}
		}
	}

	/**
	 * Tests the savepoint functionality in MySQL.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testSavepoint() throws Exception {
		if (!isRunningOnJdk131()) {
			DatabaseMetaData dbmd = this.conn.getMetaData();
	
			if (dbmd.supportsSavepoints()) {
				System.out.println("Testing SAVEPOINTs");
	
				try {
					this.conn.setAutoCommit(true);
	
					this.stmt.executeUpdate("DROP TABLE IF EXISTS testSavepoints");
					this.stmt
							.executeUpdate("CREATE TABLE testSavepoints (field1 int) TYPE=InnoDB");
	
					// Try with named save points
					this.conn.setAutoCommit(false);
					this.stmt
							.executeUpdate("INSERT INTO testSavepoints VALUES (1)");
	
					Savepoint afterInsert = this.conn.setSavepoint("afterInsert");
					this.stmt.executeUpdate("UPDATE testSavepoints SET field1=2");
	
					Savepoint afterUpdate = this.conn.setSavepoint("afterUpdate");
					this.stmt.executeUpdate("DELETE FROM testSavepoints");
	
					assertTrue("Row count should be 0",
							getRowCount("testSavepoints") == 0);
					this.conn.rollback(afterUpdate);
					assertTrue("Row count should be 1",
							getRowCount("testSavepoints") == 1);
					assertTrue("Value should be 2", "2".equals(getSingleValue(
							"testSavepoints", "field1", null).toString()));
					this.conn.rollback(afterInsert);
					assertTrue("Value should be 1", "1".equals(getSingleValue(
							"testSavepoints", "field1", null).toString()));
					this.conn.rollback();
					assertTrue("Row count should be 0",
							getRowCount("testSavepoints") == 0);
	
					// Try with 'anonymous' save points
					this.conn.rollback();
	
					this.stmt
							.executeUpdate("INSERT INTO testSavepoints VALUES (1)");
					afterInsert = this.conn.setSavepoint();
					this.stmt.executeUpdate("UPDATE testSavepoints SET field1=2");
					afterUpdate = this.conn.setSavepoint();
					this.stmt.executeUpdate("DELETE FROM testSavepoints");
	
					assertTrue("Row count should be 0",
							getRowCount("testSavepoints") == 0);
					this.conn.rollback(afterUpdate);
					assertTrue("Row count should be 1",
							getRowCount("testSavepoints") == 1);
					assertTrue("Value should be 2", "2".equals(getSingleValue(
							"testSavepoints", "field1", null).toString()));
					this.conn.rollback(afterInsert);
					assertTrue("Value should be 1", "1".equals(getSingleValue(
							"testSavepoints", "field1", null).toString()));
					this.conn.rollback();
	
					this.conn.releaseSavepoint(this.conn.setSavepoint());
				} finally {
					this.conn.setAutoCommit(true);
					this.stmt.executeUpdate("DROP TABLE IF EXISTS testSavepoints");
				}
			} else {
				System.out.println("MySQL version does not support SAVEPOINTs");
			}
		}
	}

	/**
	 * Tests the ability to set the connection collation via properties.
	 * 
	 * @throws Exception
	 *             if an error occurs or the test fails
	 */
	public void testNonStandardConnectionCollation() throws Exception {
		if (versionMeetsMinimum(4, 1)) {
			String collationToSet = "utf8_bin";
			String characterSet = "utf-8";

			Properties props = new Properties();
			props.setProperty("connectionCollation", collationToSet);
			props.setProperty("characterEncoding", characterSet);

			Connection collConn = null;
			Statement collStmt = null;
			ResultSet collRs = null;

			try {
				collConn = getConnectionWithProps(props);

				collStmt = collConn.createStatement();

				collRs = collStmt
						.executeQuery("SHOW VARIABLES LIKE 'collation_connection'");

				assertTrue(collRs.next());
				assertTrue(collationToSet.equalsIgnoreCase(collRs.getString(2)));
			} finally {
				if (collConn != null) {
					collConn.close();
				}
			}
		}
	}

	public void testDumpQueriesOnException() throws Exception {
		Properties props = new Properties();
		props.setProperty("dumpQueriesOnException", "true");
		String bogusSQL = "SELECT 1 TO BAZ";
		Connection dumpConn = getConnectionWithProps(props);

		try {
			dumpConn.createStatement().executeQuery(bogusSQL);
		} catch (SQLException sqlEx) {
			assertTrue(sqlEx.getMessage().indexOf(bogusSQL) != -1);
		}

		try {
			((com.mysql.jdbc.Connection) dumpConn).clientPrepareStatement(
					bogusSQL).executeQuery();
		} catch (SQLException sqlEx) {
			assertTrue(sqlEx.getMessage().indexOf(bogusSQL) != -1);
		}

		try {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testDumpQueriesOnException");
			this.stmt
					.executeUpdate("CREATE TABLE testDumpQueriesOnException (field1 int UNIQUE)");
			this.stmt
					.executeUpdate("INSERT INTO testDumpQueriesOnException VALUES (1)");

			PreparedStatement pStmt = dumpConn
					.prepareStatement("INSERT INTO testDumpQueriesOnException VALUES (?)");
			pStmt.setInt(1, 1);
			pStmt.executeUpdate();
		} catch (SQLException sqlEx) {
			assertTrue(sqlEx.getMessage().indexOf(
					"INSERT INTO testDumpQueriesOnException") != -1);
		} finally {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testDumpQueriesOnException");
		}

		try {
			dumpConn.prepareStatement(bogusSQL);
		} catch (SQLException sqlEx) {
			assertTrue(sqlEx.getMessage().indexOf(bogusSQL) != -1);
		}
	}

	/**
	 * Tests functionality of the ConnectionPropertiesTransform interface.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testConnectionPropertiesTransform() throws Exception {
		String transformClassName = SimpleTransformer.class.getName();

		Properties props = new Properties();

		props.setProperty(NonRegisteringDriver.PROPERTIES_TRANSFORM_KEY,
				transformClassName);

		NonRegisteringDriver driver = new NonRegisteringDriver();

		Properties transformedProps = driver
				.parseURL(BaseTestCase.dbUrl, props);

		assertTrue("albequerque".equals(transformedProps
				.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY)));
	}

	/**
	 * Tests functionality of using URLs in 'LOAD DATA LOCAL INFILE' statements.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testLocalInfileWithUrl() throws Exception {
		File infile = File.createTempFile("foo", "txt");
		infile.deleteOnExit();
		String url = infile.toURL().toExternalForm();
		FileWriter output = new FileWriter(infile);
		output.write("Test");
		output.flush();
		output.close();

		try {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testLocalInfileWithUrl");
			this.stmt
					.executeUpdate("CREATE TABLE testLocalInfileWithUrl (field1 LONGTEXT)");

			Properties props = new Properties();
			props.setProperty("allowUrlInLocalInfile", "true");

			Connection loadConn = getConnectionWithProps(props);
			Statement loadStmt = loadConn.createStatement();

			try {
				loadStmt.executeQuery("LOAD DATA LOCAL INFILE '" + url
						+ "' INTO TABLE testLocalInfileWithUrl");
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();

				throw sqlEx;
			}

			this.rs = this.stmt
					.executeQuery("SELECT * FROM testLocalInfileWithUrl");
			assertTrue(this.rs.next());
			assertTrue("Test".equals(this.rs.getString(1)));
			int count = this.stmt
					.executeUpdate("DELETE FROM testLocalInfileWithUrl");
			assertTrue(count == 1);

			StringBuffer escapedPath = new StringBuffer();
			String path = infile.getCanonicalPath();

			for (int i = 0; i < path.length(); i++) {
				char c = path.charAt(i);

				if (c == '\\') {
					escapedPath.append('\\');
				}

				escapedPath.append(c);
			}

			loadStmt.executeQuery("LOAD DATA LOCAL INFILE '"
					+ escapedPath.toString()
					+ "' INTO TABLE testLocalInfileWithUrl");
			this.rs = this.stmt
					.executeQuery("SELECT * FROM testLocalInfileWithUrl");
			assertTrue(this.rs.next());
			assertTrue("Test".equals(this.rs.getString(1)));

			try {
				loadStmt
						.executeQuery("LOAD DATA LOCAL INFILE 'foo:///' INTO TABLE testLocalInfileWithUrl");
			} catch (SQLException sqlEx) {
				assertTrue(sqlEx.getMessage() != null);
				assertTrue(sqlEx.getMessage().indexOf("FileNotFoundException") != -1);
			}

		} finally {
			this.stmt
					.executeUpdate("DROP TABLE IF EXISTS testLocalInfileWithUrl");
		}
	}

	public void testLocalInfileDisabled() throws Exception {
		createTable("testLocalInfileDisabled", "(field1 varchar(255))");
		
		File infile = File.createTempFile("foo", "txt");
		infile.deleteOnExit();
		String url = infile.toURL().toExternalForm();
		FileWriter output = new FileWriter(infile);
		output.write("Test");
		output.flush();
		output.close();
		
		Connection loadConn = getConnectionWithProps(new Properties());
		
		try {
			// have to do this after connect, otherwise it's the server
			// that's enforcing it
			((com.mysql.jdbc.Connection)loadConn).setAllowLoadLocalInfile(false);
			try {
				loadConn.createStatement().execute("LOAD DATA LOCAL INFILE '" + infile.getCanonicalPath() + "' INTO TABLE testLocalInfileDisabled");
				fail("Should've thrown an exception.");
			} catch (SQLException sqlEx) {
				assertEquals(SQLError.SQL_STATE_GENERAL_ERROR, sqlEx.getSQLState());
			}
			
			assertFalse(loadConn.createStatement().executeQuery("SELECT * FROM testLocalInfileDisabled").next());
		} finally {
			loadConn.close();
		}
	}
	
	public void testServerConfigurationCache() throws Exception {
		Properties props = new Properties();

		props.setProperty("cacheServerConfiguration", "true");
		props.setProperty("profileSQL", "true");
		props.setProperty("logFactory", "com.mysql.jdbc.log.StandardLogger");

		Connection conn1 = getConnectionWithProps(props);

		StandardLogger.saveLogsToBuffer();

		Connection conn2 = getConnectionWithProps(props);

		assertTrue("Configuration wasn't cached", StandardLogger.bufferedLog
				.toString().indexOf("SHOW VARIABLES") == -1);

		if (versionMeetsMinimum(4, 1)) {
			assertTrue("Configuration wasn't cached",
					StandardLogger.bufferedLog.toString().indexOf(
							"SHOW COLLATION") == -1);

		}
	}

	/**
	 * Tests whether or not the configuration 'useLocalSessionState' actually
	 * prevents non-needed 'set autocommit=', 'set session transaction isolation
	 * ...' and 'show variables like tx_isolation' queries.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testUseLocalSessionState() throws Exception {
		Properties props = new Properties();

		props.setProperty("useLocalSessionState", "true");
		props.setProperty("profileSQL", "true");
		props.setProperty("logFactory", "com.mysql.jdbc.log.StandardLogger");

		Connection conn1 = getConnectionWithProps(props);
		conn1.setAutoCommit(true);
		conn1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

		StandardLogger.saveLogsToBuffer();
		StandardLogger.bufferedLog.setLength(0);

		conn1.setAutoCommit(true);
		conn1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		conn1.getTransactionIsolation();

		String logAsString = StandardLogger.bufferedLog.toString();

		assertTrue(logAsString.indexOf("SET SESSION") == -1
				&& logAsString.indexOf("SHOW VARIABLES LIKE 'tx_isolation'") == -1
				&& logAsString.indexOf("SET autocommit=") == -1);

	}

	/**
	 * Tests whether re-connect with non-read-only connection can happen.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testFailoverConnection() throws Exception {

		if (!isServerRunningOnWindows()) { // windows sockets don't
			                               // work for this test
			Properties props = new Properties();
			props.setProperty("autoReconnect", "true");
			props.setProperty("failOverReadOnly", "false");
	
			// Re-build the connection information
			int firstIndexOfHost = BaseTestCase.dbUrl.indexOf("//") + 2;
			int lastIndexOfHost = BaseTestCase.dbUrl.indexOf("/", firstIndexOfHost);
	
			String hostPortPair = BaseTestCase.dbUrl.substring(firstIndexOfHost,
					lastIndexOfHost);
			System.out.println(hostPortPair);
	
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
	
			if (host == null) {
				host = "localhost"; 
			}
			
			if (st.hasMoreTokens()) {
				port = st.nextToken();
			}
	
			StringBuffer newHostBuf = new StringBuffer();
			newHostBuf.append(host);
			if (port != null) {
				newHostBuf.append(":");
				newHostBuf.append(port);
			}
			newHostBuf.append(",");
			newHostBuf.append(host);
			if (port != null) {
				newHostBuf.append(":");
				newHostBuf.append(port);
			}
	
			props
					.put(NonRegisteringDriver.HOST_PROPERTY_KEY, newHostBuf
							.toString());
	
			Connection failoverConnection = null;
	
			try {
				failoverConnection = getConnectionWithProps(props);
	
				String originalConnectionId = getSingleIndexedValueWithQuery(
						failoverConnection, 1, "SELECT connection_id()").toString();
				System.out.println("Original Connection Id = "
						+ originalConnectionId);
	
				assertTrue("Connection should not be in READ_ONLY state",
						!failoverConnection.isReadOnly());
	
				// Kill the connection
				this.stmt.executeUpdate("KILL " + originalConnectionId);
	
				// This takes a bit to occur
	
				Thread.sleep(3000);
	
				try {
					failoverConnection.createStatement().executeQuery("SELECT 1");
					fail("We expect an exception here, because the connection should be gone until the reconnect code picks it up again");
				} catch (SQLException sqlEx) {
					; // do-nothing
				}
	
				// Tickle re-connect
	
				failoverConnection.setAutoCommit(true);
	
				String newConnectionId = getSingleIndexedValueWithQuery(
						failoverConnection, 1, "SELECT connection_id()").toString();
				System.out.println("new Connection Id = " + newConnectionId);
	
				assertTrue(
						"We should have a new connection to the server in this case",
						!newConnectionId.equals(originalConnectionId));
				assertTrue("Connection should not be read-only",
						!failoverConnection.isReadOnly());
			} finally {
				if (failoverConnection != null) {
					failoverConnection.close();
				}
			}
		}
	}

	public void testCannedConfigs() throws Exception {
		String url = "jdbc:mysql:///?useConfigs=clusterBase";

		Properties cannedProps = new NonRegisteringDriver().parseURL(url, null);

		assertTrue("true".equals(cannedProps.getProperty("autoReconnect")));
		assertTrue("false".equals(cannedProps.getProperty("failOverReadOnly")));
		assertTrue("true".equals(cannedProps
				.getProperty("roundRobinLoadBalance")));

		// this will fail, but we test that too
		url = "jdbc:mysql:///?useConfigs=clusterBase,clusterBase2";

		try {
			cannedProps = new NonRegisteringDriver().parseURL(url, null);
			fail("should've bailed on that one!");
		} catch (SQLException sqlEx) {
			assertTrue(SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE
					.equals(sqlEx.getSQLState()));
		}
	}

	public void testUseOldUTF8Behavior() throws Exception {

		Properties props = new Properties();
		props.setProperty("useOldUTF8Behavior", "true");
		props.setProperty("useUnicode", "true");
		props.setProperty("characterEncoding", "UTF-8");
		props.setProperty("logFactory", "com.mysql.jdbc.log.StandardLogger");
		props.setProperty("profileSQL", "true");
		StandardLogger.saveLogsToBuffer();
		StandardLogger.bufferedLog.setLength(0);

		try {
			getConnectionWithProps(props);

			assertTrue(StringUtils.indexOfIgnoreCase(StandardLogger.bufferedLog
					.toString(), "SET NAMES utf8") == -1);
		} finally {
			StandardLogger.bufferedLog = null;
		}
	}

	/**
	 * Checks implementation of 'dontTrackOpenResources' property.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testDontTrackOpenResources() throws Exception {
		Properties props = new Properties();

		props.setProperty("dontTrackOpenResources", "true");
		Connection noTrackConn = null;
		Statement noTrackStatement = null;
		PreparedStatement noTrackPstmt = null;
		ResultSet rs2 = null;

		try {
			noTrackConn = getConnectionWithProps(props);
			noTrackStatement = noTrackConn.createStatement();
			noTrackPstmt = noTrackConn.prepareStatement("SELECT 1");
			rs2 = noTrackPstmt.executeQuery();
			rs2.next();

			this.rs = noTrackStatement.executeQuery("SELECT 1");
			this.rs.next();

			noTrackConn.close();

			// Under 'strict' JDBC requirements, these calls should fail
			// (and _do_ if dontTrackOpenResources == false)

			this.rs.getString(1);
			rs2.getString(1);
		} finally {
			if (rs2 != null) {
				rs2.close();
			}

			if (noTrackStatement != null) {
				noTrackStatement.close();
			}

			if (noTrackConn != null & !noTrackConn.isClosed()) {
				noTrackConn.close();
			}
		}
	}

	public void testPing() throws SQLException {
		Connection conn2 = getConnectionWithProps((Properties)null);

		((com.mysql.jdbc.Connection) conn2).ping();
		conn2.close();

		try {
			((com.mysql.jdbc.Connection) conn2).ping();
			fail("Should have failed with an exception");
		} catch (SQLException sqlEx) {
			// ignore for now
		}

		//
		// This feature caused BUG#8975, so check for that too!

		Properties props = new Properties();
		props.setProperty("autoReconnect", "true");

		getConnectionWithProps(props);
	}

	public void testSessionVariables() throws Exception {
		String getInitialMaxAllowedPacket = getMysqlVariable("max_allowed_packet");

		int newMaxAllowedPacket = Integer.parseInt(getInitialMaxAllowedPacket) + 1024;

		Properties props = new Properties();
		props.setProperty("sessionVariables", "max_allowed_packet="
				+ newMaxAllowedPacket);
		props.setProperty("profileSQL", "true");

		Connection varConn = getConnectionWithProps(props);

		assertTrue(!getInitialMaxAllowedPacket.equals(getMysqlVariable(varConn,
				"max_allowed_packet")));
	}

	/**
	 * Tests setting profileSql on/off in the span of one connection.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testSetProfileSql() throws Exception {
		((com.mysql.jdbc.Connection) this.conn).setProfileSql(false);
		stmt.executeQuery("SELECT 1");
		((com.mysql.jdbc.Connection) this.conn).setProfileSql(true);
		stmt.executeQuery("SELECT 1");
	}

	public void testCreateDatabaseIfNotExist() throws Exception {
		if (isAdminConnectionConfigured()) {
			Properties props = new Properties();
			props.setProperty("createDatabaseIfNotExist", "true");
			props.setProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY,
					"testcreatedatabaseifnotexists");

			Connection newConn = getAdminConnectionWithProps(props);
			newConn.createStatement().executeUpdate(
					"DROP DATABASE testcreatedatabaseifnotexists");
		}
	}
    
    /**
     * Tests if gatherPerfMetrics works.
     * 
     * @throws Exception if the test fails
     */
    public void testGatherPerfMetrics() throws Exception {
        if(versionMeetsMinimum(4, 1)) {
            try {
                Properties props = new Properties();
                props.put("autoReconnect", "true");
                props.put("relaxAutoCommit", "true");
                props.put("logSlowQueries", "true");
                props.put("slowQueryThresholdMillis", "2000");
                // these properties were reported as the cause of NullPointerException
                props.put("gatherPerfMetrics", "true"); 
                props.put("reportMetricsIntervalMillis", "3000"); 
                
                Connection conn1 = getConnectionWithProps(props);
                Statement stmt1 = conn1.createStatement();
                ResultSet rs1 = stmt1.executeQuery("SELECT 1");
                rs1.next();
                conn1.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
                fail();
            }
        }
    }

    /**
     * Tests if useCompress works.
     * 
     * @throws Exception if the test fails
     */
    public void testUseCompress() throws Exception {
        Properties props = new Properties();
        props.put("useCompression", "true");
        props.put("traceProtocol", "true");
        Connection conn1 = getConnectionWithProps(props);
        Statement stmt1 = conn1.createStatement();
        ResultSet rs1 = stmt1.executeQuery("SELECT VERSION()");
        rs1.next();
        rs1.getString(1);
        stmt1.close();
        conn1.close();
    }
    
    /**
     * Tests feature of "localSocketAddress", by enumerating local IF's and
     * trying each one in turn. This test might take a long time to run, since
     * we can't set timeouts if we're using localSocketAddress. We try and keep
     * the time down on the testcase by spawning the checking of each interface
     * off into separate threads.
     * 
     * @throws Exception if the test can't use at least one of the local machine's
     *                   interfaces to make an outgoing connection to the server.
     */
    public void testLocalSocketAddress() throws Exception {
    	if (isRunningOnJdk131()) { 
    		return;
    	}
    	
    	Enumeration allInterfaces = NetworkInterface.getNetworkInterfaces();
    	
    	
    	SpawnedWorkerCounter counter = new SpawnedWorkerCounter();
    	
    	List allChecks = new ArrayList();
    	
    	while (allInterfaces.hasMoreElements()) {
    		NetworkInterface intf = (NetworkInterface)allInterfaces.nextElement();
    		
    		Enumeration allAddresses = intf.getInetAddresses();

    		allChecks.add(new LocalSocketAddressCheckThread(allAddresses, counter));
    	}
    	
    	counter.setWorkerCount(allChecks.size());
    	
    	for (Iterator it = allChecks.iterator(); it.hasNext();) {
    		LocalSocketAddressCheckThread t = (LocalSocketAddressCheckThread)it.next();
    		t.start();
    	}
    	
    	// Wait for tests to complete....
    	synchronized (counter) {
    	
    		while (counter.workerCount > 0 /* safety valve */) {
    		
    			counter.wait();

    			if (counter.workerCount == 0) {
    				System.out.println("Done!");
    				break;
    			}
    		}
    	}
    	
    	boolean didOneWork = false;
    	boolean didOneFail = false;
    	
    	for (Iterator it = allChecks.iterator(); it.hasNext();) {
    		LocalSocketAddressCheckThread t = (LocalSocketAddressCheckThread)it.next();

    		if (t.atLeastOneWorked) {
    			didOneWork = true;
    			
    			break;
    		} else {
    			if (!didOneFail) {
    				didOneFail = true;
    			}
    		}
    	}
    	
    	assertTrue("At least one connection was made with the localSocketAddress set", didOneWork);
    	
    	NonRegisteringDriver d = new NonRegisteringDriver();
    	
    	String hostname = d.host(d.parseURL(dbUrl, null));
    	
    	if (!hostname.startsWith(":") && !hostname.startsWith("localhost")) {
    		
    		int indexOfColon = hostname.indexOf(":");
    		
    		if (indexOfColon != -1) {
    			hostname = hostname.substring(0, indexOfColon);
    		}
    		
    		boolean isLocalIf = false;
    		
    		isLocalIf = (null != NetworkInterface.getByName(hostname));
    		
    		if (!isLocalIf) {
    			try {
    				isLocalIf = (null != NetworkInterface.getByInetAddress(InetAddress.getByName(hostname)));
    			} catch (Throwable t) {
    				isLocalIf = false;
    			}
    		}
    		
    		if (!isLocalIf) {
    			assertTrue("At least one connection didn't fail with localSocketAddress set", didOneFail);
    		}
    	}
    }
    
    class SpawnedWorkerCounter {
    	private int workerCount = 0;
    	
    	synchronized void setWorkerCount(int i) {
    		workerCount = i;
    	}
    	
    	synchronized void decrementWorkerCount() {
    		workerCount--;
    		notify();
    	}
    }
    
    class LocalSocketAddressCheckThread extends Thread {
    	boolean atLeastOneWorked = false;
    	Enumeration allAddresses = null;
    	SpawnedWorkerCounter counter = null;
    	
    	LocalSocketAddressCheckThread(Enumeration e, SpawnedWorkerCounter c) {
    		allAddresses = e;
    		counter = c;
    	}
    	
    	public void run() {
    		
    		while (allAddresses.hasMoreElements()) {
    			InetAddress addr = (InetAddress)allAddresses.nextElement();
    			
    			try {
    				Properties props = new Properties();
    				props.setProperty("localSocketAddress", addr.getHostAddress());
    				props.setProperty("connectTimeout", "2000");
    				getConnectionWithProps(props).close();
    				
    				atLeastOneWorked = true;
    				
    				break;
    			} catch (SQLException sqlEx) {
    				// ignore, we're only seeing if one of these tests succeeds
    			}
    		}
    		
    		counter.decrementWorkerCount();
    	}
    }
    
    public void testUsageAdvisorTooLargeResultSet() throws Exception {
    	Connection uaConn = null;
    	
    	PrintStream stderr = System.err;
    	
    	StringBuffer logBuf = new StringBuffer();
    	
    	StandardLogger.bufferedLog = logBuf;
    	
    	try {
    		Properties props = new Properties();
    		props.setProperty("useUsageAdvisor", "true");
    		props.setProperty("resultSetSizeThreshold", "4");
    		props.setProperty("logger", "StandardLogger");
    		
    		uaConn = getConnectionWithProps(props);
    		
    		assertTrue("Result set threshold message not present", 
    				logBuf.toString().indexOf("larger than \"resultSetSizeThreshold\" of 4 rows") != -1);
    	} finally {
    		System.setErr(stderr);
    		
    		closeMemberJDBCResources();
    		
    		if (uaConn != null) {
    			uaConn.close();
    		}
    	}
    }
    
    public void testUseLocalSessionStateRollback() throws Exception {
    	if (!versionMeetsMinimum(5, 0, 0)) {
    		return;
    	}
    	
    	Properties props = new Properties();
    	props.setProperty("useLocalSessionState", "true");
    	props.setProperty("profileSQL", "true");
    	
    	StringBuffer buf = new StringBuffer();
    	StandardLogger.bufferedLog = buf;
    	
    	createTable("testUseLocalSessionState", "(field1 varchar(32)) ENGINE=InnoDB");
    	
    	Connection localStateConn = null;
    	Statement localStateStmt = null;
    	
    	try {
    		localStateConn = getConnectionWithProps(props);
        	localStateStmt = localStateConn.createStatement();
        	
	    	localStateConn.setAutoCommit(false);
	    	localStateStmt.executeUpdate("INSERT INTO testUseLocalSessionState VALUES ('abc')");
	    	localStateConn.rollback();
	    	localStateConn.rollback();
	    	localStateStmt.executeUpdate("INSERT INTO testUseLocalSessionState VALUES ('abc')");
	    	localStateConn.commit();
	    	localStateConn.commit();
	    	localStateStmt.close();
    	} finally {
    		StandardLogger.bufferedLog = null;
    		 
    		if (localStateStmt != null) {
    			localStateStmt.close();
    		}
    		
    		if (localStateConn != null) {
    			localStateConn.close();
    		}
    	}
    	
    	int rollbackCount = 0;
    	int rollbackPos = 0;
    	
    	String searchIn = buf.toString();
    	
    	while (rollbackPos != -1) {
    		rollbackPos = searchIn.indexOf("rollback", rollbackPos);
    		
    		if (rollbackPos != -1) {
    			rollbackPos += "rollback".length();
    			rollbackCount++;
    		}
    	}
    	
    	assertEquals(1, rollbackCount);
    	
    	int commitCount = 0;
    	int commitPos = 0;
    	
    	// space is important here, we don't want to count "autocommit"
    	while (commitPos != -1) {
    		commitPos = searchIn.indexOf(" commit", commitPos);
    		
    		if (commitPos != -1) {
    			commitPos += " commit".length();
    			commitCount++;
    		}
    	}
    	
    	assertEquals(1, commitCount);
    }
    
    /**
     * Checks if setting useCursorFetch to "true" automatically
     * enables server-side prepared statements.
     */
     
    public void testCouplingOfCursorFetch() throws Exception {
    	if (!versionMeetsMinimum(5, 0)) {
    		return;
    	}
    	
    	Connection fetchConn = null;
    	
    	try {
    		Properties props = new Properties();
    		props.setProperty("useServerPrepStmts", "false"); // force the issue
    		props.setProperty("useCursorFetch", "true");
    		fetchConn = getConnectionWithProps(props);
    		assertEquals("com.mysql.jdbc.ServerPreparedStatement",
    				fetchConn.prepareStatement("SELECT 1").getClass().getName());
    	} finally {
    		if (fetchConn != null) {
    			fetchConn.close();
    		}
    	}
    }
}
