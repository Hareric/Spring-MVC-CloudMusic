package model.db;

import java.sql.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.ResultSetMetaData;

/**
 * @author Eric_Chan
 * @version 2016.10.14
 */
public class DbHelper {
	private String ipAddress = "127.0.0.1";
	private int port = 3306;
	private String user = "";
	private String pwd = "";
	private String dbName = "";
	private Connection conn;
	private static DbHelper instance = null; // 单例

	/**
	 * 检查是否导入 mysql-connector-java.jar
	 */
	private DbHelper() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out
					.println("没有正确导入 connector.jar\n  下载地址http://dev.mysql.com/downloads/connector/j/");
		}
	}

	/**
	 * 获得唯一连接器
	 * 
	 * @return DbConnection
	 */
	public static DbHelper getInstance() {
		if (DbHelper.instance == null) {
			instance = new DbHelper();
			return DbHelper.instance;
		} else {
			return DbHelper.instance;
		}
	}

	/**
	 * 连接数据库
	 */
	private void connect() {
		String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8", this.ipAddress,
				this.port, this.dbName);
		try {
			this.conn = DriverManager.getConnection(url, this.user, this.pwd);
		} catch (SQLException e) {
			System.out.println("连接失败\n");
			e.printStackTrace();
		}
	}

	/**
	 * 提供参数 并连接数据库 默认为本地数据库，端口为3306
	 * 
	 * @param user
	 *            用户名
	 * @param pwd
	 *            密码
	 * @param dbName
	 *            数据库名
	 */
	public void connSQL(String user, String pwd, String dbName) {
		this.user = user;
		this.pwd = pwd;
		this.dbName = dbName;
		this.connect();
		System.out.println("数据库连接成功  conn-------------" + conn + '\n');
		this.free(this.conn, null, null);
	}

	public void connSQL(String user, String pwd, String dbName, int port) {
		this.port = port;
		this.connSQL(user, pwd, dbName);
	}

	public void connSQL(String user, String pwd, String dbName, String ipAddress) {
		this.ipAddress = ipAddress;
		this.connSQL(user, pwd, dbName);
	}

	public void connSQL(String user, String pwd, String dbName,
			String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.connSQL(user, pwd, dbName);
	}

	/**
	 * 将表内数据输出至控制台
	 * 
	 * @param tableName
	 *            表名
	 * @throws Exception
	 */
	public void showTable(String tableName) {
		this.connect();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			DbHelper.showResultSet(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.free(this.conn, stmt, rs);
		}
	}

	/**
	 * 查【Query】 无参查找
	 * 
	 * @param sql
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String sql) {
		this.connect();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			this.free(conn, stmt, rs);
		}
		return rs;
	}

	/**
	 * 查【Query】 有参查找
	 * 
	 * @param sql
	 * @param obj
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String sql, Object... obj) {
		this.connect();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < obj.length; i++) {
				pstmt.setObject(i + 1, obj[i]);
			}
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			this.free(conn, pstmt, rs);
		}
		return rs;
	}
	
	/**
	 * 更新【update】 无参更新
	 * 
	 * @param sql
	 * @return int
	 */
	public int executeUpdate(String sql) {
		this.connect();
		Statement stmt = null;
		int rs = 0;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			this.free(conn, stmt, null);
		}
		return rs;
	}

	/**
	 * 更新【update】 有参更新
	 * 
	 * @param sql
	 * @param obj
	 * @return int
	 */
	public int executeUpdate(String sql, Object... obj) {
		this.connect();
		PreparedStatement pstmt = null;
		int rs = 0;
		try {
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < obj.length; i++) {
				pstmt.setObject(i + 1, obj[i]);
			}
			rs = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			this.free(conn, pstmt, null);
		}
		return rs;
	}
	
	/**
	 * 将ResultSet内的数据输出至控制台
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	public static void showResultSet(ResultSet rs){
		ResultSetMetaData rsmd;
		try {
			rsmd = (ResultSetMetaData) rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			// 输出列名
			for (int i = 1; i <= columnCount; i++) {
				System.out.print(rsmd.getColumnName(i));
				System.out.print("(" + rsmd.getColumnTypeName(i) + ")");
				System.out.print(" | ");
			}
			System.out.println();
			// 输出数据
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.print(rs.getString(i) + "  |  ");
				}
				System.out.println();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 判断记录是否存在
	 * 
	 * @param sql
	 * @return Boolean
	 */
	public Boolean isExist(String sql) {
		this.connect();
		Statement stmt = null;
		Boolean isEx = false;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			rs.last();
			isEx = rs.getRow() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.free(conn, stmt, rs);
		}
		return isEx;
	}

	/**
	 * 判断记录是否存在
	 * 
	 * @param sql
	 * @return Boolean
	 */
	public Boolean isExist(String sql, Object... obj) {
		this.connect();
		PreparedStatement pstmt = null;
		Boolean isEx = false;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < obj.length; i++) {
				pstmt.setObject(i + 1, obj[i]);
			}
			rs = pstmt.executeQuery();
			rs.last();
			isEx = rs.getRow() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.free(conn, pstmt, rs);
		}
		return isEx;
	}

	/**
	 * 用来释放所有数据资源
	 * 
	 * @param conn
	 * @param stmt
	 * @param rs
	 * @throws SQLException
	 */
	public void free(Connection conn, Statement stmt, ResultSet rs) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将ResultSet 转化成 json
	 * @param rs ResultSet
	 * @return String json
	 */
	public static String resultSetToJson(ResultSet rs){
		JSONArray array = new JSONArray();     
		try {
			// 获取列数  
			ResultSetMetaData metaData;
			metaData = (ResultSetMetaData) rs.getMetaData();
			int columnCount = metaData.getColumnCount();  
		    
			   // 遍历ResultSet中的每条数据  
			    while (rs.next()) {  
			        JSONObject jsonObj = new JSONObject();  
			         
			        // 遍历每一列  
			        for (int i = 1; i <= columnCount; i++) {  
			            String columnName =metaData.getColumnLabel(i);  
			            String value = rs.getString(columnName);  
			            jsonObj.put(columnName, value);  
			        }   
			        array.put(jsonObj);   
			    }  
			    
			   return array.toString();  
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(JSONException e){
			e.printStackTrace();
		}
		return "error";
	}

	public static void main(String args[]) throws SQLException {
		DbHelper connector = DbHelper.getInstance();
		connector.connSQL("root", "1q2w3e", "student", 3307);
		connector.showTable("student");
		ResultSet rs = connector.executeQuery(
				"SELECT * FROM student WHERE st_name=? and st_Password=?",
				"Jack", "1234");
		DbHelper.showResultSet(rs);
		Boolean isEx = connector.isExist(
				"SELECT * FROM student WHERE st_name=? and st_Password=?",
				"Jack", "1234");
		System.out.println(isEx);
		connector.close();
	}

}
