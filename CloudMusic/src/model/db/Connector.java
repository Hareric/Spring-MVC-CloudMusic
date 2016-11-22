package model.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置文件 生成数据库连接
 * @author Eric_Chan
 *
 */
public class Connector {
	private static DbHelper connector = null;

	public void loadConfig() {
		// 读取数据库配置文件
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("dbConfig.properties");  
		Properties p = new Properties();
		try {
			p.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String ipAddress = p.getProperty("ip");
		int port = Integer.parseInt(p.getProperty("port"));
		String user = p.getProperty("user");
		String pwd = p.getProperty("pwd");
		String dbName = p.getProperty("dbName");
		// 根据提供的参数连接数据库
		Connector.connector = DbHelper.getInstance();
		Connector.connector.connSQL(user, pwd, dbName,ipAddress, port);
	}
	
	public static DbHelper getInstance(){
		if(Connector.connector==null){
			new Connector().loadConfig();
		}
		return Connector.connector;
	}
	
//	public static void main(String args[]){
//		DbHelper db = Connector.getInstance();
//		db.showTable("app_Music");
//	}
}
