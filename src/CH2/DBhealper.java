package CH2;
import java.sql.*;

/**
* @author Eric_Chan
* @version 2016.10.10
*/
public class DBhealper 
{
	private String ipAddress = "127.0.0.1";
	private int port = 3306;
	private String user = "";
	private String pwd = "";
	private String dbName = "";
	private Connection conn;
	private static DBhealper instance = null;  // 单例
	
	/**
	 * 检查是否导入 mysql-connector-java.jar
	 */
	private DBhealper()
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
	 * 获得唯一连接器
	 * @return DbConnection
	 */
	public static DBhealper getInstance()
	{
		if (DBhealper.instance == null)
		{
			instance = new DBhealper();
			return DBhealper.instance;
		}
		else
		{
			return DBhealper.instance;
		}
	}
	
	/**
	 * 连接数据库
	 */
	private void connect() throws SQLException
	{
		String url = String.format("jdbc:mysql://%s:%s/%s",this.ipAddress, this.port, this.dbName);
		try
		{
			this.conn = DriverManager.getConnection(url, this.user, this.pwd);
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
	public void connSQL(String user, String pwd, String dbName) throws SQLException
	{
		this.user = user;
		this.pwd = pwd;
		this.dbName = dbName;
		this.connect();
		System.out.println("连接成功\nconn-------------" + conn + '\n');
		this.free(this.conn, null, null);
	}
	public void connSQL(String user, String pwd, String dbName, int port) throws SQLException
	{
		this.port = port;
		this.connSQL(user, pwd, dbName);
	}
	public void connSQL(String user, String pwd, String dbName, String ipAddress) throws SQLException
	{
		this.ipAddress = ipAddress;
		this.connSQL(user, pwd, dbName);
	}
	public void connSQL(String user, String pwd, String dbName,String ipAddress, int port) throws SQLException
	{
		this.ipAddress = ipAddress;
		this.port = port;
		this.connSQL(user, pwd, dbName);
	}

	/**
	 * 将表内数据输出至控制台
	 * @param tableName 表名
	 * @throws Exception
	 */
	public void showTable(String tableName) throws SQLException
	{
		this.connect();
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT * FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			DBhealper.showResultSet(rs);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			this.free(this.conn, stmt, rs);
		}
	}

	/**
     * 查【Query】
     * 无参查找
     * @param sql
     * @return ResultSet
     */
    public ResultSet executeQuery(String sql) throws SQLException
    {
        this.connect();
        Statement stmt = null;
        ResultSet rs = null;
        try 
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            this.free(conn, stmt, rs);
        }
        return rs;
    }

    /**
     * 查【Query】
     * 有参查找
     * @param sql
     * @param obj
     * @return ResultSet
     */
    public ResultSet executeQuery(String sql, Object... obj) throws SQLException
    {
    	this.connect();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try 
        {
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < obj.length; i++) 
            {
                pstmt.setObject(i + 1, obj[i]);
            }
            rs = pstmt.executeQuery();
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            this.free(conn, pstmt, rs);
        }
        return rs;
    }
    
	/**
	 * 将ResultSet内的数据输出至控制台
	 * @param rs
	 * @throws SQLException
	 */
	public static void showResultSet(ResultSet rs) throws SQLException
	{
	    ResultSetMetaData rsmd = rs.getMetaData();   
	    int columnCount = rsmd.getColumnCount();   
	    // 输出列名   
	    for (int i=1; i<=columnCount; i++)
	    {   
	        System.out.print(rsmd.getColumnName(i));   
	        System.out.print("(" + rsmd.getColumnTypeName(i) + ")");   
	        System.out.print(" | ");   
	    }   
	    System.out.println();   
	    // 输出数据   
	    while (rs.next())
	    {   
	        for (int i=1; i<=columnCount; i++)
	        {   
	            System.out.print(rs.getString(i) + "  |  ");   
	        }   
	        System.out.println();   
	    }  
	}
	
	/**
     * 判断记录是否存在
     *
     * @param sql
     * @return Boolean
     */
    public Boolean isExist(String sql) throws SQLException
    {
        this.connect();
        Statement stmt = null;
        Boolean isEx = false;
        ResultSet rs = null;
        try 
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.last();
            isEx = rs.getRow()>0; 
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        finally
        {
        	this.free(conn, stmt, rs);
        }
        return isEx;
    }

    /**
     * 判断记录是否存在
     * @param sql
     * @return Boolean
     */
    public Boolean isExist(String sql, Object... obj) throws SQLException
    {
    	this.connect();
        PreparedStatement pstmt = null;
        Boolean isEx = false;
        ResultSet rs = null;
        try 
        {
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < obj.length; i++) 
            {
                pstmt.setObject(i + 1, obj[i]);
            }
            rs = pstmt.executeQuery();
            rs.last();
            isEx = rs.getRow()>0;
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        finally
        {
            this.free(conn, pstmt, rs);
        }
        return isEx;
    }
	/**
	 * 用来释放所有数据资源
	 * @param conn 
	 * @param stmt
	 * @param rs
	 * @throws SQLException
	 */
	public void free(Connection conn, Statement stmt, ResultSet rs) throws SQLException
	{
		if (conn!=null)
		{
			try
			{
				conn.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		if (stmt!=null)
		{
			try
			{
				stmt.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		if (rs!=null)
		{
			try
			{
				rs.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void close()
	{
		try
		{
			this.conn.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String args[]) throws SQLException
	{
		DBhealper connector = DBhealper.getInstance();
		connector.connSQL("root", "1q2w3e", "student", 3307);
		// connector.showTable("student");
		//ResultSet rs = connector.executeQuery("SELECT * FROM student WHERE st_name=? and st_Password=?", "Jack", "1234");
		//DBhealper.showResultSet(rs);
		Boolean isEx = connector.isExist("SELECT * FROM student WHERE st_name=? and st_Password=?", "Jack", "1234");
		System.out.println(isEx);
		connector.close();
	}
	

}