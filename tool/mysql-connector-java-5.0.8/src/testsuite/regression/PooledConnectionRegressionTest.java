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
package testsuite.regression;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import junit.framework.Test;
import junit.framework.TestSuite;
import testsuite.BaseTestCase;

import com.mysql.jdbc.PacketTooBigException;
import com.mysql.jdbc.jdbc2.optional.ConnectionWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

/**
 * Tests a PooledConnection implementation provided by a JDBC driver. Test case
 * provided by Johnny Macchione from bug database record BUG#884. According to
 * the JDBC 2.0 specification:
 * 
 * <p>
 * "Each call to PooledConnection.getConnection() must return a newly
 * constructed Connection object that exhibits the default Connection behavior.
 * Only the most recent Connection object produced from a particular
 * PooledConnection is open. An existing Connection object is automatically
 * closed, if the getConnection() method of its associated Pooled-Connection is
 * called again, before it has been explicitly closed by the application. This
 * gives the application server a way to �take away� a Connection from the
 * application if it wishes, and give it out to someone else. This capability
 * will not likely be used frequently in practice."
 * </p>
 * 
 * <p>
 * "When the application calls Connection.close(), an event is triggered that
 * tells the connection pool it can recycle the physical database connection. In
 * other words, the event signals the connection pool that the PooledConnection
 * object which originally produced the Connection object generating the event
 * can be put back in the connection pool."
 * </p>
 * 
 * <p>
 * "A Connection-EventListener will also be notified when a fatal error occurs,
 * so that it can make a note not to put a bad PooledConnection object back in
 * the cache when the application finishes using it. When an error occurs, the
 * ConnectionEventListener is notified by the JDBC driver, just before the
 * driver throws an SQLException to the application to notify it of the same
 * error. Note that automatic closing of a Connection object as discussed in the
 * previous section does not generate a connection close event."
 * </p>
 * The JDBC 3.0 specification states the same in other words:
 * 
 * <p>
 * "The Connection.close method closes the logical handle, but the physical
 * connection is maintained. The connection pool manager is notified that the
 * underlying PooledConnection object is now available for reuse. If the
 * application attempts to reuse the logical handle, the Connection
 * implementation throws an SQLException."
 * </p>
 * 
 * <p>
 * "For a given PooledConnection object, only the most recently produced logical
 * Connection object will be valid. Any previously existing Connection object is
 * automatically closed when the associated PooledConnection.getConnection
 * method is called. Listeners (connection pool managers) are not notified in
 * this case. This gives the application server a way to take a connection away
 * from a client. This is an unlikely scenario but may be useful if the
 * application server is trying to force an orderly shutdown."
 * </p>
 * 
 * <p>
 * "A connection pool manager shuts down a physical connection by calling the
 * method PooledConnection.close. This method is typically called only in
 * certain circumstances: when the application server is undergoing an orderly
 * shutdown, when the connection cache is being reinitialized, or when the
 * application server receives an event indicating that an unrecoverable error
 * has occurred on the connection."
 * </p>
 * Even though the specification isn't clear about it, I think it is no use
 * generating a close event when calling the method PooledConnection.close(),
 * even if a logical Connection is open for this PooledConnection, bc the
 * PooledConnection will obviously not be returned to the pool.
 * 
 * @author fcr
 */
public final class PooledConnectionRegressionTest extends BaseTestCase {
	private ConnectionPoolDataSource cpds;

	// Count nb of closeEvent.
	private int closeEventCount;

	// Count nb of connectionErrorEvent
	private int connectionErrorEventCount;

	/**
	 * Creates a new instance of ProgressPooledConnectionTest
	 * 
	 * @param testname
	 *            DOCUMENT ME!
	 */
	public PooledConnectionRegressionTest(String testname) {
		super(testname);
	}

	/**
	 * Set up test case before a test is run.
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void setUp() throws Exception {
		super.setUp();

		// Reset event count.
		this.closeEventCount = 0;
		this.connectionErrorEventCount = 0;

		MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();

		ds.setURL(BaseTestCase.dbUrl);

		this.cpds = ds;
	}

	/**
	 * Runs all test cases in this test suite
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(PooledConnectionRegressionTest.class);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return a test suite composed of this test case.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(PooledConnectionRegressionTest.class);

		return suite;
	}

	/**
	 * After the test is run.
	 */
	public void tearDown() {
		this.cpds = null;
	}

	/**
	 * Tests fix for BUG#7136 ... Statement.getConnection() returning physical
	 * connection instead of logical connection.
	 */
	public void testBug7136() {
		final ConnectionEventListener conListener = new ConnectionListener();
		PooledConnection pc = null;
		this.closeEventCount = 0;

		try {
			pc = this.cpds.getPooledConnection();

			pc.addConnectionEventListener(conListener);

			Connection conn = pc.getConnection();

			Connection connFromStatement = conn.createStatement()
					.getConnection();

			// This should generate a close event.

			connFromStatement.close();

			assertEquals("One close event should've been registered", 1,
					this.closeEventCount);

			this.closeEventCount = 0;

			conn = pc.getConnection();

			Connection connFromPreparedStatement = conn.prepareStatement(
					"SELECT 1").getConnection();

			// This should generate a close event.

			connFromPreparedStatement.close();

			assertEquals("One close event should've been registered", 1,
					this.closeEventCount);

		} catch (SQLException ex) {
			fail(ex.toString());
		} finally {
			if (pc != null) {
				try {
					pc.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Test the nb of closeEvents generated when a Connection is reclaimed. No
	 * event should be generated in that case.
	 */
	public void testConnectionReclaim() {
		final ConnectionEventListener conListener = new ConnectionListener();
		PooledConnection pc = null;
		final int NB_TESTS = 5;

		try {
			pc = this.cpds.getPooledConnection();

			pc.addConnectionEventListener(conListener);

			for (int i = 0; i < NB_TESTS; i++) {
				Connection conn = pc.getConnection();

				try {
					// Try to reclaim connection.
					System.out.println("Before connection reclaim.");

					conn = pc.getConnection();

					System.out.println("After connection reclaim.");
				} finally {
					if (conn != null) {
						System.out.println("Before connection.close().");

						// This should generate a close event.
						conn.close();

						System.out.println("After connection.close().");
					}
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			fail(ex.toString());
		} finally {
			if (pc != null) {
				try {
					System.out.println("Before pooledConnection.close().");

					// This should not generate a close event.
					pc.close();

					System.out.println("After pooledConnection.close().");
				} catch (SQLException ex) {
					ex.printStackTrace();
					fail(ex.toString());
				}
			}
		}

		assertEquals("Wrong nb of CloseEvents: ", NB_TESTS,
				this.closeEventCount);
	}

	/**
	 * Tests that PacketTooLargeException doesn't clober the connection.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
	public void testPacketTooLargeException() throws Exception {
		final ConnectionEventListener conListener = new ConnectionListener();
		PooledConnection pc = null;

		pc = this.cpds.getPooledConnection();

		pc.addConnectionEventListener(conListener);

		try {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testPacketTooLarge");
			this.stmt
					.executeUpdate("CREATE TABLE testPacketTooLarge(field1 LONGBLOB)");

			Connection connFromPool = pc.getConnection();
			PreparedStatement pstmtFromPool = ((ConnectionWrapper) connFromPool)
					.clientPrepare("INSERT INTO testPacketTooLarge VALUES (?)");

			this.rs = this.stmt
					.executeQuery("SHOW VARIABLES LIKE 'max_allowed_packet'");
			this.rs.next();

			int maxAllowedPacket = this.rs.getInt(2);

			int numChars = (int) (maxAllowedPacket * 1.2);

			pstmtFromPool.setBinaryStream(1, new BufferedInputStream(
					new FileInputStream(newTempBinaryFile(
							"testPacketTooLargeException", numChars))),
					numChars);

			try {
				pstmtFromPool.executeUpdate();
				fail("Expecting PacketTooLargeException");
			} catch (PacketTooBigException ptbe) {
				// We're expecting this one...
			}

			// This should still work okay, even though the last query on the
			// same
			// connection didn't...
			connFromPool.createStatement().executeQuery("SELECT 1");

			assertTrue(this.connectionErrorEventCount == 0);
			assertTrue(this.closeEventCount == 0);
		} finally {
			this.stmt.executeUpdate("DROP TABLE IF EXISTS testPacketTooLarge");
		}
	}

	/**
	 * Test the nb of closeEvents generated by a PooledConnection. A
	 * JDBC-compliant driver should only generate 1 closeEvent each time
	 * connection.close() is called.
	 */
	public void testCloseEvent() {
		final ConnectionEventListener conListener = new ConnectionListener();
		PooledConnection pc = null;
		final int NB_TESTS = 5;

		try {
			pc = this.cpds.getPooledConnection();

			pc.addConnectionEventListener(conListener);

			for (int i = 0; i < NB_TESTS; i++) {
				Connection pConn = pc.getConnection();

				System.out.println("Before connection.close().");

				// This should generate a close event.
				pConn.close();

				System.out.println("After connection.close().");
			}
		} catch (SQLException ex) {
			fail(ex.toString());
		} finally {
			if (pc != null) {
				try {
					System.out.println("Before pooledConnection.close().");

					// This should not generate a close event.
					pc.close();

					System.out.println("After pooledConnection.close().");
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
		assertEquals("Wrong nb of CloseEvents: ", NB_TESTS,
				this.closeEventCount);
	}

	/**
	 * Listener for PooledConnection events.
	 */
	private final class ConnectionListener implements ConnectionEventListener {
		/** */
		public void connectionClosed(ConnectionEvent event) {
			PooledConnectionRegressionTest.this.closeEventCount++;
			System.out
					.println(PooledConnectionRegressionTest.this.closeEventCount
							+ " - Connection closed.");
		}

		/** */
		public void connectionErrorOccurred(ConnectionEvent event) {
			PooledConnectionRegressionTest.this.connectionErrorEventCount++;
			System.out.println("Connection error: " + event.getSQLException());
		}
	}
}
