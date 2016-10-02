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
package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionProperties;
import com.mysql.jdbc.NonRegisteringDriver;

import java.io.PrintWriter;
import java.io.Serializable;

import java.sql.SQLException;

import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import javax.sql.DataSource;

/**
 * A JNDI DataSource for a Mysql JDBC connection
 * 
 * @author Mark Matthews
 */
public class MysqlDataSource extends ConnectionProperties implements
		DataSource, Referenceable, Serializable {
	/** The driver to create connections with */
	protected static com.mysql.jdbc.Driver mysqlDriver = null;

	static {
		try {
			mysqlDriver = (com.mysql.jdbc.Driver) Class.forName(
					"com.mysql.jdbc.Driver").newInstance();
		} catch (Exception E) {
			throw new RuntimeException(
					"Can not load Driver class com.mysql.jdbc.Driver");
		}
	}

	/** Log stream */
	protected PrintWriter logWriter = null;

	/** Database Name */
	protected String databaseName = null;

	/** Character Encoding */
	protected String encoding = null;

	/** Hostname */
	protected String hostName = null;

	/** Password */
	protected String password = null;

	/** The profileSql property */
	protected String profileSql = "false";

	/** The JDBC URL */
	protected String url = null;

	/** User name */
	protected String user = null;

	/** Should we construct the URL, or has it been set explicitly */
	protected boolean explicitUrl = false;

	/** Port number */
	protected int port = 3306;

	/**
	 * Default no-arg constructor for Serialization
	 */
	public MysqlDataSource() {
	}

	/**
	 * Creates a new connection using the already configured username and
	 * password.
	 * 
	 * @return a connection to the database
	 * 
	 * @throws SQLException
	 *             if an error occurs
	 */
	public java.sql.Connection getConnection() throws SQLException {
		return getConnection(this.user, this.password);
	}

	/**
	 * Creates a new connection with the given username and password
	 * 
	 * @param userID
	 *            the user id to connect with
	 * @param password
	 *            the password to connect with
	 * 
	 * @return a connection to the database
	 * 
	 * @throws SQLException
	 *             if an error occurs
	 */
	public java.sql.Connection getConnection(String userID, String pass)
			throws SQLException {
		Properties props = new Properties();

		if (userID != null) {
			props.setProperty(NonRegisteringDriver.USER_PROPERTY_KEY, userID);
		}

		if (pass != null) {
			props.setProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY, pass);
		}

		exposeAsProperties(props);

		return getConnection(props);
	}

	/**
	 * Sets the database name.
	 * 
	 * @param dbName
	 *            the name of the database
	 */
	public void setDatabaseName(String dbName) {
		this.databaseName = dbName;
	}

	/**
	 * Gets the name of the database
	 * 
	 * @return the name of the database for this data source
	 */
	public String getDatabaseName() {
		return (this.databaseName != null) ? this.databaseName : "";
	}

	/**
	 * Sets the log writer for this data source.
	 * 
	 * @see javax.sql.DataSource#setLogWriter(PrintWriter)
	 */
	public void setLogWriter(PrintWriter output) throws SQLException {
		this.logWriter = output;
	}

	/**
	 * Returns the log writer for this data source
	 * 
	 * @return the log writer for this data source
	 */
	public java.io.PrintWriter getLogWriter() {
		return this.logWriter;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param seconds
	 *            DOCUMENT ME!
	 * 
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
	}

	/**
	 * Returns the login timeout
	 * 
	 * @return the login timeout
	 */
	public int getLoginTimeout() {
		return 0;
	}

	/**
	 * Sets the password
	 * 
	 * @param pass
	 *            the password
	 */
	public void setPassword(String pass) {
		this.password = pass;
	}

	/**
	 * Sets the database port.
	 * 
	 * @param p
	 *            the port
	 */
	public void setPort(int p) {
		this.port = p;
	}

	/**
	 * Returns the port number
	 * 
	 * @return the port number
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Sets the port number
	 * 
	 * @param p
	 *            the port
	 * 
	 * @see #setPort
	 */
	public void setPortNumber(int p) {
		setPort(p);
	}

	/**
	 * Returns the port number
	 * 
	 * @return the port number
	 */
	public int getPortNumber() {
		return getPort();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param ref
	 *            DOCUMENT ME!
	 * 
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	public void setPropertiesViaRef(Reference ref) throws SQLException {
		super.initializeFromRef(ref);
	}

	/**
	 * Required method to support this class as a <CODE>Referenceable</CODE>.
	 * 
	 * @return a Reference to this data source
	 * 
	 * @throws NamingException
	 *             if a JNDI error occurs
	 */
	public Reference getReference() throws NamingException {
		String factoryName = "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory";
		Reference ref = new Reference(getClass().getName(), factoryName, null);
		ref.add(new StringRefAddr(NonRegisteringDriver.USER_PROPERTY_KEY,
				getUser()));
		ref.add(new StringRefAddr(NonRegisteringDriver.PASSWORD_PROPERTY_KEY,
				this.password));
		ref.add(new StringRefAddr("serverName", getServerName()));
		ref.add(new StringRefAddr("port", "" + getPort()));
		ref.add(new StringRefAddr("databaseName", getDatabaseName()));
		ref.add(new StringRefAddr("url", getUrl()));
		ref.add(new StringRefAddr("explicitUrl", String
				.valueOf(this.explicitUrl)));

		//
		// Now store all of the 'non-standard' properties...
		//
		try {
			storeToRef(ref);
		} catch (SQLException sqlEx) {
			throw new NamingException(sqlEx.getMessage());
		}

		return ref;
	}

	/**
	 * Sets the server name.
	 * 
	 * @param serverName
	 *            the server name
	 */
	public void setServerName(String serverName) {
		this.hostName = serverName;
	}

	/**
	 * Returns the name of the database server
	 * 
	 * @return the name of the database server
	 */
	public String getServerName() {
		return (this.hostName != null) ? this.hostName : "";
	}

	//
	// I've seen application servers use both formats
	// URL or url (doh)
	//

	/**
	 * Sets the URL for this connection
	 * 
	 * @param url
	 *            the URL for this connection
	 */
	public void setURL(String url) {
		setUrl(url);
	}

	/**
	 * Returns the URL for this connection
	 * 
	 * @return the URL for this connection
	 */
	public String getURL() {
		return getUrl();
	}

	/**
	 * This method is used by the app server to set the url string specified
	 * within the datasource deployment descriptor. It is discovered using
	 * introspection and matches if property name in descriptor is "url".
	 * 
	 * @param url
	 *            url to be used within driver.connect
	 */
	public void setUrl(String url) {
		this.url = url;
		this.explicitUrl = true;
	}

	/**
	 * Returns the JDBC URL that will be used to create the database connection.
	 * 
	 * @return the URL for this connection
	 */
	public String getUrl() {
		if (!this.explicitUrl) {
			String builtUrl = "jdbc:mysql://";
			builtUrl = builtUrl + getServerName() + ":" + getPort() + "/"
					+ getDatabaseName();

			return builtUrl;
		}

		return this.url;
	}

	/**
	 * Sets the user ID.
	 * 
	 * @param userID
	 *            the User ID
	 */
	public void setUser(String userID) {
		this.user = userID;
	}

	/**
	 * Returns the configured user for this connection
	 * 
	 * @return the user for this connection
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Creates a connection using the specified properties.
	 * 
	 * @param props
	 *            the properties to connect with
	 * 
	 * @return a connection to the database
	 * 
	 * @throws SQLException
	 *             if an error occurs
	 */
	protected java.sql.Connection getConnection(Properties props)
			throws SQLException {
		String jdbcUrlToUse = null;

		if (!this.explicitUrl) {
			StringBuffer jdbcUrl = new StringBuffer("jdbc:mysql://");

			if (this.hostName != null) {
				jdbcUrl.append(this.hostName);
			}

			jdbcUrl.append(":");
			jdbcUrl.append(this.port);
			jdbcUrl.append("/");

			if (this.databaseName != null) {
				jdbcUrl.append(this.databaseName);
			}

			jdbcUrlToUse = jdbcUrl.toString();
		} else {
			jdbcUrlToUse = this.url;
		}

		return mysqlDriver.connect(jdbcUrlToUse, props);
	}
}
