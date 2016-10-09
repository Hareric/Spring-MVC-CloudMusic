package CH2;



import java.sql.*;

/**
 * 单例模式创建mysql连接器
 * @author 20141002419
 *
 */
public class DbConnection 
{
	private String ipAddress = "127.0.0.1";
	private int port = 3306;
	private String user = "";
	private String pwd = "";
	private String dbName = "";
	private java.sql.Connection conn;
	private static DbConnection instance = null;  // 单例
	
	/**
	 * 检查是否导入 mysql-connector-java.jar
	 */
	private DbConnection()
	{
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e) 
		{
			System.out.println("没有正确导入 connector.jar\n  下载地址http://dev.mysql.com/downloads/connector/j/");
		} 
	}
	
	/**
	 * 连接数据库
	 */
	private void connect()
	{
		String url = String.format("jdbc:mysql://%s:%s/%s",this.ipAddress, this.port, this.dbName);
		try
		{
			conn = DriverManager.getConnection(url, this.user, this.pwd);
			System.out.println("连接成功\n conn-------------" + conn + '\n');
		}
		catch (SQLException e) 
		{
			System.out.println("连接失败\n");
			e.printStackTrace();
		}
	}
	
	/**
	 * 提供参数 并连接数据库 默认为本地数据库，端口为3306
	 * @param user 用户名
	 * @param pwd 密码
	 * @param dbName 数据库名
	 */
	public void connSQL(String user, String pwd, String dbName)
	{
		this.user = user;
		this.pwd = pwd;
		this.dbName = dbName;
		this.connect();
	}
	
	public void connSQL(String user, String pwd, String dbName, int port)
	{
		this.port = port;
		this.connSQL(user, pwd, dbName);
	}
	
	public void connSQL(String user, String pwd, String dbName, String ipAddress)
	{
		this.ipAddress = ipAddress;
		this.connSQL(user, pwd, dbName);
	}
	
	public void connSQL(String user, String pwd, String dbName,String ipAddress, int port)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		this.connSQL(user, pwd, dbName);
	}

	public void showTable(String tableName) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			
			String sql = "SELECT * FROM ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tableName);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				String name = rs.getString("c_name");
				String credit = rs.getString("c_credit");
				String content = rs.getString("c_content");
				System.out.println("name:" + name + "  credit:" + credit + "  content:" + content);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			rs.close();
	        pstmt.close();
		}
		
		
	}
	
	public void close() throws SQLException
	{
		this.conn.close();
	}
	
	/**
	 * 获得唯一连接器
	 * @return DbConnection
	 */
	public static DbConnection getInstance()
	{
		if (DbConnection.instance == null)
		{
			instance = new DbConnection();
			return DbConnection.instance;
		}
		else
		{
			return DbConnection.instance;
		}
	}
	
	public static void main(String args[]) throws Exception
	{
		DbConnection connector = DbConnection.getInstance();
		connector.connSQL("root", "1q2w3e", "student", 3307);
		connector.showTable("course");
		connector.close();

	}
	

}