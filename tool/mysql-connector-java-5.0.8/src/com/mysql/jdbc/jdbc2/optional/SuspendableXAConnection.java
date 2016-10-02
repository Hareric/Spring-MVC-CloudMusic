package com.mysql.jdbc.jdbc2.optional;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.mysql.jdbc.Connection;

public class SuspendableXAConnection extends MysqlPooledConnection implements
XAConnection, XAResource {

	public SuspendableXAConnection(Connection connection) {
		super(connection);
		this.underlyingConnection = connection;
	}

	private static final Map XIDS_TO_PHYSICAL_CONNECTIONS = 
		new HashMap();
	
	private Xid currentXid;
	
	private XAConnection currentXAConnection;
	private XAResource currentXAResource;
	
	private Connection underlyingConnection;
	
	private static synchronized XAConnection findConnectionForXid(Connection connectionToWrap, Xid xid) 
		throws SQLException {
		// TODO: check for same GTRID, but different BQUALs...MySQL doesn't allow this yet
		
		// Note, we don't need to check for XIDs here, because MySQL itself will complain
		// with a XAER_NOTA if need be.
		
		XAConnection conn = (XAConnection)XIDS_TO_PHYSICAL_CONNECTIONS.get(xid);

		if (conn == null) {
			conn = new MysqlXAConnection(connectionToWrap, 
					connectionToWrap.getLogXaCommands());
		}
		
		return conn;
	}
	
	private static synchronized void removeXAConnectionMapping(Xid xid) {
		XIDS_TO_PHYSICAL_CONNECTIONS.remove(xid);
	}
	
	private synchronized void switchToXid(Xid xid) throws XAException {
		if (xid == null) {
			throw new XAException();
		}
		
		try {
			if (!xid.equals(this.currentXid)) {
				XAConnection toSwitchTo = findConnectionForXid(this.underlyingConnection, xid);
				this.currentXAConnection = toSwitchTo;
				this.currentXid = xid;
				this.currentXAResource = toSwitchTo.getXAResource();
			}
		} catch (SQLException sqlEx) {
			throw new XAException();
		}
	}
	
	public XAResource getXAResource() throws SQLException {
		return this;
	}

	public void commit(Xid xid, boolean arg1) throws XAException {
		switchToXid(xid);
		this.currentXAResource.commit(xid, arg1);
		removeXAConnectionMapping(xid);
	}

	public void end(Xid xid, int arg1) throws XAException {
		switchToXid(xid);
		this.currentXAResource.end(xid, arg1);
	}

	public void forget(Xid xid) throws XAException {
		switchToXid(xid);
		this.currentXAResource.forget(xid);
		// remove?
		removeXAConnectionMapping(xid);
	}

	public int getTransactionTimeout() throws XAException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isSameRM(XAResource xaRes) throws XAException {
		return xaRes == this;
	}

	public int prepare(Xid xid) throws XAException {
		switchToXid(xid);
		return this.currentXAResource.prepare(xid);
	}

	public Xid[] recover(int flag) throws XAException {
		return MysqlXAConnection.recover(this.underlyingConnection, flag);
	}

	public void rollback(Xid xid) throws XAException {
		switchToXid(xid);
		this.currentXAResource.rollback(xid);
		removeXAConnectionMapping(xid);
	}

	public boolean setTransactionTimeout(int arg0) throws XAException {
		// TODO Auto-generated method stub
		return false;
	}

	public void start(Xid xid, int arg1) throws XAException {
		switchToXid(xid);
		
		if (arg1 != XAResource.TMJOIN) {
			this.currentXAResource.start(xid, arg1);
			
			return;
		}
		
		//
		// Emulate join, by using resume on the same physical connection
		//
		
		this.currentXAResource.start(xid, XAResource.TMRESUME);
	}

	public synchronized java.sql.Connection getConnection() throws SQLException {
		if (this.currentXAConnection == null) {
			return getConnection(false, true);
		}
			
		return this.currentXAConnection.getConnection();
	}

	public void close() throws SQLException {
		if (this.currentXAConnection == null) {
			super.close();
		} else {
			removeXAConnectionMapping(this.currentXid);
			this.currentXAConnection.close();
		}
	}
}
